package com.reactnativegooglearcore.effects;

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
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRenderer;
import com.reactnativegooglearcore.common.helpers.CameraPermissionHelper;
import com.reactnativegooglearcore.common.helpers.DisplayRotationHelper;
import com.reactnativegooglearcore.common.helpers.SaveBitmap;
import com.reactnativegooglearcore.common.helpers.SnackbarHelper;
import com.reactnativegooglearcore.common.helpers.TrackingStateHelper;
import com.reactnativegooglearcore.common.rendering.BackgroundRenderer;
import com.reactnativegooglearcore.common.rendering.ObjectRenderer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraEffect implements GLSurfaceView.Renderer {
  private static final String TAG = CameraEffect.class.getSimpleName();

  public GLSurfaceView surfaceView;

  private Session session;
  public final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  public DisplayRotationHelper displayRotationHelper;
  public TrackingStateHelper trackingStateHelper;

  public final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  public final AugmentedFaceRenderer augmentedFaceRenderer = new AugmentedFaceRenderer();
  public ObjectRenderer noseObject = new ObjectRenderer();
  public final ObjectRenderer rightEarObject = new ObjectRenderer();
  public final ObjectRenderer leftEarObject = new ObjectRenderer();

  // Temporary matrix allocated here to reduce number of allocations for each frame.
  public final float[] noseMatrix = new float[16];
  public final float[] rightEarMatrix = new float[16];
  public final float[] leftEarMatrix = new float[16];
  public static final float[] DEFAULT_COLOR = new float[] {0f, 0f, 0f, 0f};
  private ReactApplicationContext reactContext;

  private boolean requestedCapture = false;
  private boolean drawFaceMakeup = false;
  private boolean drawNose = false;
  private boolean drawLeftEar = false;
  private boolean drawRightEar = false;
  private boolean isRecording = false;

  private String DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

  SaveBitmap saveBitmap = new SaveBitmap();

  private String noseObj = "models/nose.obj";
  private String noseObjTexture = "models/nose_fur.png";
  private String leftEarObj = "models/forehead_left.obj";
  private String leftEarObjTexture = "models/ear_fur.png";
  private String rightEarObj = "models/forehead_left.obj";
  private String rightEarObjTexture = "models/ear_fur.png";
  private String faceMakeupTexture = "models/freckles.png";

  private boolean isObjChanged = false;

  public CameraEffect(ReactApplicationContext context, GLSurfaceView glSurfaceView) {
    this.reactContext = context;
    this.surfaceView = glSurfaceView;
    surfaceView.setPreserveEGLContextOnPause(true);
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

  // ARCore requires camera permissions to operate. If we did not yet obtain runtime
  // permission on Android M and above, now is a good time to ask the user for it.
  private boolean requestCameraPermission() {
    if (!CameraPermissionHelper.hasCameraPermission(reactContext.getCurrentActivity())) {
      CameraPermissionHelper.requestCameraPermission(reactContext.getCurrentActivity());
      return false;
    } else {
      return true;
    }
  }

  // Create the session and configure it to use a front-facing (selfie) camera.
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

  private boolean resumeSession() {
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

  private void createNoseObject() throws IOException {
    noseObject.createOnGlThread(reactContext, noseObj, noseObjTexture);
    noseObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
    noseObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);
  }

  private void createRightEarObject() throws IOException {
    rightEarObject.createOnGlThread(reactContext, rightEarObj, rightEarObjTexture);
    rightEarObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
    rightEarObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);
  }

  private void createLeftEarObject() throws IOException {
    leftEarObject.createOnGlThread(reactContext, leftEarObj, leftEarObjTexture);
    leftEarObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
    leftEarObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);
  }

  private void createFaceMakeup() throws IOException {
    augmentedFaceRenderer.createOnGlThread(reactContext, faceMakeupTexture);
    augmentedFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
  }

  public void setDrawFaceMakeup(boolean drawFaceMakeup) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.drawFaceMakeup = drawFaceMakeup;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setDrawLeftEar(boolean drawLeftEar) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.drawLeftEar = drawLeftEar;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setDrawRightEar(boolean drawRightEar) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.drawRightEar = drawRightEar;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setDrawNose(boolean drawNose) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.drawNose = drawNose;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setLeftEarObj(String leftEarObj) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.leftEarObj = leftEarObj;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setLeftEarObjTexture(String leftEarObjTexture) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.leftEarObjTexture = leftEarObjTexture;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setFaceMakeupTexture(String faceMakeupTexture) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.faceMakeupTexture = faceMakeupTexture;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setNoseObj(String noseObj) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.noseObj = noseObj;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setNoseObjTexture(String noseObjTexture) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.noseObjTexture = noseObjTexture;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setRightEarObj(String rightEarObj) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.rightEarObj = rightEarObj;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setRightEarObjTexture(String rightEarObjTexture) {
    if (session != null) {
      try {
        surfaceView.onPause();
        this.rightEarObjTexture = rightEarObjTexture;
        this.isObjChanged = true;
        surfaceView.onResume();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void replaceObjects() {
    try {
      createFaceMakeup();
      createNoseObject();
      createRightEarObject();
      createLeftEarObject();
    } catch (IOException e) {
      Log.e(TAG, "Failed to read an asset file", e);
    }
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(reactContext);

      if (!faceMakeupTexture.isEmpty()) createFaceMakeup();
      if (!noseObj.isEmpty()) createNoseObject();
      if (!rightEarObj.isEmpty()) createRightEarObject();
      if (!leftEarObj.isEmpty()) createLeftEarObject();

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
      replaceObjects();
      return;
    }

// Clear screen to notify driver it should not load any pixels from previous frame.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    if (session == null) {
      return;
    }
    // Notify ARCore session that the view size changed so that the perspective matrix and
    // the video background can be properly adjusted.
    displayRotationHelper.updateSessionIfNeeded(session);

    try {
      session.setCameraTextureName(backgroundRenderer.getTextureId());

      // Obtain the current frame from ARSession. When the configuration is set to
      // UpdateMode.BLOCKING (it is by default), this will throttle the rendering to the
      // camera framerate.
      Frame frame = session.update();
      Camera camera = frame.getCamera();

      // Get projection matrix.
      float[] projectionMatrix = new float[16];
      camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);

      // Get camera matrix and draw.
      float[] viewMatrix = new float[16];
      camera.getViewMatrix(viewMatrix, 0);

      // Compute lighting from average intensity of the image.
      // The first three components are color scaling factors.
      // The last one is the average pixel intensity in gamma space.
      final float[] colorCorrectionRgba = new float[4];
      frame.getLightEstimate().getColorCorrection(colorCorrectionRgba, 0);

      // If frame is ready, render camera preview image to the GL surface.
      backgroundRenderer.draw(frame);

      // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
      trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

      // ARCore's face detection works best on upright faces, relative to gravity.
      // If the device cannot determine a screen side aligned with gravity, face
      // detection may not work optimally.
      Collection<AugmentedFace> faces = session.getAllTrackables(AugmentedFace.class);
      for (AugmentedFace face : faces) {
        if (face.getTrackingState() != TrackingState.TRACKING) {
          break;
        }

        float scaleFactor = 1.0f;

        // Face objects use transparency so they must be rendered back to front without depth write.
        GLES20.glDepthMask(false);

        // Each face's region poses, mesh vertices, and mesh normals are updated every frame.

        // 1. Render the face mesh first, behind any 3D objects attached to the face regions.
        float[] modelMatrix = new float[16];
        face.getCenterPose().toMatrix(modelMatrix, 0);
        if (drawFaceMakeup) {
          augmentedFaceRenderer.draw(
            projectionMatrix, viewMatrix, modelMatrix, colorCorrectionRgba, face);
        }

        // 2. Next, render the 3D objects attached to the forehead.
        if (drawRightEar) {
          face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT).toMatrix(rightEarMatrix, 0);
          rightEarObject.updateModelMatrix(rightEarMatrix, scaleFactor);
          rightEarObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR);
        }
        if (drawLeftEar) {
          face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT).toMatrix(leftEarMatrix, 0);
          leftEarObject.updateModelMatrix(leftEarMatrix, scaleFactor);
          leftEarObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR);
        }

        // 3. Render the nose last so that it is not occluded by face mesh or by 3D objects attached
        // to the forehead regions.
        if (drawNose && noseObject != null) {
          face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP).toMatrix(noseMatrix, 0);
          noseObject.updateModelMatrix(noseMatrix, scaleFactor);
          noseObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR);
        }
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
