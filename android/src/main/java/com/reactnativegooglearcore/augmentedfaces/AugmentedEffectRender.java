package com.reactnativegooglearcore.augmentedfaces;

import android.app.KeyguardManager;
import android.content.Context;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.RecordingConfig;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.RecordingFailedException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.reactnativegooglearcore.R;
import com.reactnativegooglearcore.common.helpers.CameraPermissionHelper;
import com.reactnativegooglearcore.common.helpers.DisplayRotationHelper;
import com.reactnativegooglearcore.common.helpers.SaveBitmap;
import com.reactnativegooglearcore.common.helpers.SnackbarHelper;
import com.reactnativegooglearcore.common.helpers.TrackingStateHelper;
import com.reactnativegooglearcore.common.rendering.BackgroundRenderer;
import com.reactnativegooglearcore.effects.CreateEffect;
import com.reactnativegooglearcore.effects.Object3D;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AugmentedEffectRender implements GLSurfaceView.Renderer {
  private static final String TAG = AugmentedEffectRender.class.getSimpleName();

  public GLSurfaceView surfaceView;

  private Session session;
  private String sessionStatus = "RESUMED";
  public final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  public DisplayRotationHelper displayRotationHelper;
  public TrackingStateHelper trackingStateHelper;

  public final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();

  private ReactApplicationContext reactContext;

  private boolean requestedCapture = false;
  private boolean isRecording = false;

  private String DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

  SaveBitmap saveBitmap = new SaveBitmap();

  private boolean isObjChanged = true;

  public Map<String,AugmentedFaceInterface> effects = new HashMap<>();
  private String effectKey = "";

  public AugmentedEffectRender(ReactApplicationContext context, GLSurfaceView glSurfaceView) {
    this.reactContext = context;
    this.surfaceView = glSurfaceView;
    inflateLayout();
    setupRender();
  }

  public void setCameraEffectDir(String dir) {
    Log.d("setImagesDir", dir);
    DIRECTORY += "/" + dir;
  }

  public void setDir(String dir) {
    setCameraEffectDir(dir);
    saveBitmap.setDIRECTORY(dir);
  }
  private void cleanObjects() {
    if (effects.size() > 0) {
      effects.forEach((s, effect) -> {
        effect.cleanObjects();
      });
    }
  }
  public void setEffect(String effectKey) {
    cleanObjects();
    if (!effectKey.isEmpty()) {
      if (effects.size() > 0 && !effects.containsKey(effectKey)) throw new NullPointerException("Cannot choose a key that not exist on effects");
      isObjChanged = true;
    }
    this.effectKey = effectKey;
  }

  public void setEffects(String jsonString) {
    try {
      this.effects.clear();
      JSONArray objArray = new JSONArray(jsonString);

      for (int index = 0; index < objArray.length(); index++) {

        JSONObject jsonObject = objArray.getJSONObject(index);
        String key = jsonObject.getString("key");
        JSONArray datas = jsonObject.getJSONArray("effect");
        ArrayList<Object3D> arrayList = new ArrayList<>();

        for (int index2 = 0; index2 < datas.length(); index2++) {
          JSONObject data = datas.getJSONObject(index2);

          Object3D object3D = new Object3D();
          object3D.object = data.getString("object");
          object3D.texture = data.getString("texture");
          object3D.regionType = AugmentedFaceRegions.RegionType.valueOf(data.getString("region"));

          arrayList.add(object3D);
        }
        CreateEffect createEffect = new CreateEffect(reactContext, arrayList);
        this.effects.put(key, createEffect);
      }
      isObjChanged = true;
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }

  public void capture() {
    this.requestedCapture = true;
  }

  private void setIsRecording(boolean isRecording) {
    this.isRecording = isRecording;
  }

  private void setupRender() {
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);
  }

  private void inflateLayout() {
    this.trackingStateHelper = new TrackingStateHelper(reactContext.getCurrentActivity());
    this.displayRotationHelper = new DisplayRotationHelper(reactContext);
  }

  private boolean requestCameraPermission() {
    if (!CameraPermissionHelper.hasCameraPermission(reactContext.getCurrentActivity())) {
      CameraPermissionHelper.requestCameraPermission(reactContext.getCurrentActivity());
      return false;
    } else {
      return true;
    }
  }

  private Response configureFrontCamera() {
    Exception exception = null;
    String message = null;

    try {
      session = new Session(reactContext, EnumSet.noneOf(Session.Feature.class));
      CameraConfigFilter cameraConfigFilter = new CameraConfigFilter(session);
      cameraConfigFilter.setFacingDirection(CameraConfig.FacingDirection.FRONT);
      List<CameraConfig> cameraConfigs = session.getSupportedCameraConfigs(cameraConfigFilter);
      if (!cameraConfigs.isEmpty()) {
        // Element 0 contains the camera config that best matches the session feature
        // and filter settings.
        session.setCameraConfig(cameraConfigs.get(0));
      } else {
        message = "This device does not have a front-facing (selfie) camera";
        exception = new UnavailableDeviceNotCompatibleException(message);
      }

      Config config = new Config(session);
      config.setAugmentedFaceMode(Config.AugmentedFaceMode.MESH3D);
      session.configure(config);

    } catch (UnavailableArcoreNotInstalledException e) {
      message = "Please install ARCore";
      exception = e;
    } catch (UnavailableApkTooOldException e) {
      message = "Please update ARCore";
      exception = e;
    } catch (UnavailableSdkTooOldException e) {
      message = "Please update this app";
      exception = e;
    } catch (UnavailableDeviceNotCompatibleException e) {
      message = "This device does not support AR";
      exception = e;
    } catch (Exception e) {
      message = "Failed to create AR session";
      exception = e;
    }

    Response response = new Response();
    response.setExcerption(exception);
    response.setMessage(message);
    return response;
  }

  public boolean resumeSession() {
    try {
      if (session != null) {
        if (surfaceView == null) {
          CoordinatorLayout coordinatorLayout = reactContext
            .getCurrentActivity().findViewById(R.id.containerSurfaceview);

          if (coordinatorLayout == null) {
            throw new NullPointerException("view.containerSurfaceview");
          }
          this.surfaceView = coordinatorLayout.findViewById(R.id.surfaceview);
          if (surfaceView == null) {
            throw new NullPointerException("Surfaceview Cannot Be A null Point");
          }
        }
        session.resume();
      }
      return true;
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(reactContext.getCurrentActivity(), "Camera not available. Try restarting the app.");
      session = null;
      return false;
    }
  }

  public void start() {
    if (session != null) return;
    final boolean cameraPermission = requestCameraPermission();
    if (!cameraPermission) return;

    Response response = this.configureFrontCamera();
    if (response.message != null) {
      messageSnackbarHelper.showError(reactContext.getCurrentActivity(), response.message);
      if (response.excerption != null) {
        Log.e(TAG, "Exception creating session", response.excerption);
      }
      return;
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    final boolean resumed = resumeSession();
    if (resumed) {
      surfaceView.onResume();
      displayRotationHelper.onResume();
    }
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    try {
      backgroundRenderer.createOnGlThread(reactContext);
    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    GLES20.glViewport(0, 0, width, height);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    if (sessionStatus == "STOPPED") return;
    KeyguardManager myKM = (KeyguardManager) reactContext.getSystemService(Context.KEYGUARD_SERVICE);
    if( !myKM.inKeyguardRestrictedInputMode() && session != null && sessionStatus == "PAUSED") {
      sessionStatus = "RESUMED";
      resumeSession();
    } else if (sessionStatus == "RESUMED" && myKM.inKeyguardRestrictedInputMode() && session != null) {
      sessionStatus = "PAUSED";
    }
    if (isObjChanged && !effectKey.isEmpty()) {
      isObjChanged = effects.containsKey(effectKey) ? false : true;
      if (effects.size() > 0 && isObjChanged == false) {
        effects.get(effectKey).createObjects();
      }
      return;
    }

    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }

    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      Frame frame = session.update();
      Camera camera = frame.getCamera();

      float[] projectionMatrix = new float[16];
      camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);

      float[] viewMatrix = new float[16];
      camera.getViewMatrix(viewMatrix, 0);

      final float[] colorCorrectionRgba = new float[4];
      float[] modelMatrix = new float[16];

      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);
      backgroundRenderer.draw(frame);
      trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

      if (effectKey.isEmpty() || effects.size() == 0) return;

      GLES20.glDepthMask(false);


      Collection<AugmentedFace> faces = session.getAllTrackables(AugmentedFace.class);
      for (AugmentedFace face : faces) {
        if (face.getTrackingState() != TrackingState.TRACKING) {
          break;
        }

        face.getCenterPose().toMatrix(modelMatrix, 0);

        AugmentedFaceInterface effect = effects.get(effectKey);
        if (effect.requireTexture() == true) {
          effect.drawTexture(face, projectionMatrix, viewMatrix, modelMatrix, colorCorrectionRgba);
        }

        effect.draw(face, projectionMatrix, viewMatrix, colorCorrectionRgba);
      }
      if (requestedCapture && isRecording == false) {
        requestedCapture = false;
        saveBitmap.setReactContext(reactContext);
        saveBitmap.setSurfaceView(surfaceView);
        saveBitmap.save(gl);
      }
    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    } finally {
      GLES20.glDepthMask(true);
    }
  }

  public void startRecording(Promise promise) {
    if (isRecording == false) {
      Random generator = new Random();
      int n = 10000;
      n = generator.nextInt(n);
      String videoName = "Video-" + n + ".mp4";
      File myDir = new File(DIRECTORY);
      if (!myDir.exists()) {
        myDir.mkdirs();
      }

      File file = new File(myDir, videoName);
      if (file.exists()) file.delete();
      Uri destination = Uri.fromFile(file);
      RecordingConfig recordingConfig =
        new RecordingConfig(session)
          .setMp4DatasetUri(destination)
          .setAutoStopOnPause(true);
      try {
        session.startRecording(recordingConfig);
        setIsRecording(true);
      } catch (RecordingFailedException e) {
        Log.e(TAG, "Failed to start recording", e);
        promise.resolve(false);
        return;
      }
      try {
        final boolean cameraAvailable = requestCameraPermission();
        if (cameraAvailable) {
          session.resume();
          promise.resolve(true);
        } else {
          promise.resolve(false);
        }
      } catch (CameraNotAvailableException e) {
        Log.e(TAG, "Failed to start recording", e);
        promise.resolve(false);
      }
    } else {
      promise.resolve(false);
    }
  }
  public void stopRecording(Promise promise) {
    if (isRecording == true) {
      try {
        session.stopRecording();
        setIsRecording(false);
        promise.resolve(false);
      } catch (RecordingFailedException e) {
        Log.e(TAG, "Failed to stop recording", e);
      }
    } else {
      promise.resolve(false);
    }
  }
  public void getRecordingStatus(Promise promise) {
    switch (session.getRecordingStatus()) {
      case OK:
        promise.resolve("STARTED");
        break;
      case NONE:
        promise.resolve("STOPPED");
        break;
      case IO_ERROR:
        promise.resolve("FAILED");
    }
  }
  public void pauseSession() {
    if (session != null) {
      sessionStatus = "PAUSED";
      session.pause();
    }
  }

  public void stopSession() {
    if (session != null) {
      sessionStatus = "STOPPED";
      session.close();

    }
  }
}
class Response {
  public String message = "";
  public Exception excerption;
  public void setMessage(String value) {
    this.message = value;
  }
  public void setExcerption(Exception value) {
    this.excerption = value;
  }
}
