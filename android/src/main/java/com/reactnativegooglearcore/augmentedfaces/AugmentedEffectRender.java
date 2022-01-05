package com.reactnativegooglearcore.augmentedfaces;

import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

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
import com.reactnativegooglearcore.common.helpers.CameraPermissionHelper;
import com.reactnativegooglearcore.common.helpers.DisplayRotationHelper;
import com.reactnativegooglearcore.common.helpers.SaveBitmap;
import com.reactnativegooglearcore.common.helpers.SnackbarHelper;
import com.reactnativegooglearcore.common.helpers.TrackingStateHelper;
import com.reactnativegooglearcore.common.rendering.BackgroundRenderer;
import com.reactnativegooglearcore.effects.BeardEffect;
import com.reactnativegooglearcore.effects.EyeStarEffect;
import com.reactnativegooglearcore.effects.FoxEffect;
import com.reactnativegooglearcore.effects.SukunaEffect;
import com.reactnativegooglearcore.effects.SuperSayajinHairEffect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class AugmentedEffectRender implements GLSurfaceView.Renderer {
  private static final String TAG = AugmentedEffectRender.class.getSimpleName();

  public GLSurfaceView surfaceView;

  private Session session;
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

  public ArrayList<AugmentedFaceInterface> effects = new ArrayList<>();
  private int effectIndex = 0;

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
  public void setEffectIndex(int effectIndex) {
    int totalIndexes = effects.size() - 1;
    if (totalIndexes >= effectIndex) {
      isObjChanged = true;
      this.effectIndex = effectIndex;
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
      session.resume();
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

  private void createEffects() {
    FoxEffect foxEffect = new FoxEffect(reactContext);
    EyeStarEffect eyeStarEffect = new EyeStarEffect(reactContext);
    BeardEffect beardEffect = new BeardEffect(reactContext);
    SuperSayajinHairEffect superSayajinHairEffect = new SuperSayajinHairEffect(reactContext);
    SukunaEffect sukunaEffect = new SukunaEffect(reactContext);

    effects.add(foxEffect);
    effects.add(eyeStarEffect);
    effects.add(beardEffect);
    effects.add(superSayajinHairEffect);
    effects.add(sukunaEffect);
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    try {
      backgroundRenderer.createOnGlThread(reactContext);
      createEffects();
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
    if (isObjChanged) {
      isObjChanged = false;
      effects.get(effectIndex).createObjects();
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

      GLES20.glDepthMask(false);

      Collection<AugmentedFace> faces = session.getAllTrackables(AugmentedFace.class);
      for (AugmentedFace face : faces) {
        if (face.getTrackingState() != TrackingState.TRACKING) {
          break;
        }

        face.getCenterPose().toMatrix(modelMatrix, 0);
        AugmentedFaceInterface effect = effects.get(effectIndex);
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
      session.pause();
    }
  }

  public void stopSession() {
    if (session != null) {
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
