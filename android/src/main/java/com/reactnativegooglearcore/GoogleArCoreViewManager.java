package com.reactnativegooglearcore;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Camera;
import com.google.ar.core.CameraConfig;
import com.google.ar.core.CameraConfigFilter;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRenderer;
import com.reactnativegooglearcore.common.helpers.CameraPermissionHelper;
import com.reactnativegooglearcore.common.helpers.DisplayRotationHelper;
import com.reactnativegooglearcore.common.helpers.SnackbarHelper;
import com.reactnativegooglearcore.common.helpers.TrackingStateHelper;
import com.reactnativegooglearcore.common.rendering.BackgroundRenderer;
import com.reactnativegooglearcore.common.rendering.ObjectRenderer;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GoogleArCoreViewManager extends ViewGroupManager<CoordinatorLayout> implements GLSurfaceView.Renderer {
  private static final String TAG = GoogleArCoreViewManager.class.getSimpleName();

  public static final String REACT_CLASS = "GoogleArCoreView";
  private ReactApplicationContext reactContext;
  private CoordinatorLayout container;
  private CoordinatorLayout containerSurfaceview;
  private View view;

  private GLSurfaceView surfaceView;

  private Session session;
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;
  private TrackingStateHelper trackingStateHelper;

  private final BackgroundRenderer backgroundRenderer = new BackgroundRenderer();
  private final AugmentedFaceRenderer augmentedFaceRenderer = new AugmentedFaceRenderer();
  private final ObjectRenderer noseObject = new ObjectRenderer();
  private final ObjectRenderer rightEarObject = new ObjectRenderer();
  private final ObjectRenderer leftEarObject = new ObjectRenderer();
  // Temporary matrix allocated here to reduce number of allocations for each frame.
  private final float[] noseMatrix = new float[16];
  private final float[] rightEarMatrix = new float[16];
  private final float[] leftEarMatrix = new float[16];
  private static final float[] DEFAULT_COLOR = new float[] {0f, 0f, 0f, 0f};

  public GoogleArCoreViewManager(ReactApplicationContext reactContext) {
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public CoordinatorLayout createViewInstance(ThemedReactContext context) {
    view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet, null);
    if (view == null) {
      throw new NullPointerException("Layout not found");
    } else {
      CoordinatorLayout defaultView = (CoordinatorLayout) this.view;
      CoordinatorLayout coordinatorLayout = defaultView.findViewById(R.id.container);
      this.container = coordinatorLayout;
      coordinatorLayout = defaultView.findViewById(R.id.containerSurfaceview);
      if (coordinatorLayout == null) {
        throw new NullPointerException("view.containerSurfaceview");
      }
      this.containerSurfaceview = coordinatorLayout;
      coordinatorLayout = this.containerSurfaceview;
      if (coordinatorLayout == null) {
        throw new NullPointerException("containerSurfaceview");
      }
      this.surfaceView = coordinatorLayout.findViewById(R.id.surfaceview);
      if (surfaceView == null) {
        throw new NullPointerException("Surfaceview Cannot Be A null Point");
      }

      container.bringToFront();

      inflateLayout();

      return defaultView;
    }
  }

  private void inflateLayout() {
    this.trackingStateHelper = new TrackingStateHelper(reactContext.getCurrentActivity());
    this.displayRotationHelper = new DisplayRotationHelper(reactContext);
    setupRender();
    start();
  }
  private void setupRender() {
    surfaceView.setPreserveEGLContextOnPause(true);
    surfaceView.setEGLContextClientVersion(2);
    surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0); // Alpha used for plane blending.
    surfaceView.setRenderer(this);
    surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    surfaceView.setWillNotDraw(false);
  }

  @Override
  public void addView(CoordinatorLayout parent, View child, int index) {
    if (container.getChildCount() > 2) {
      throw new NullPointerException("GoogleArCore Not Accept More Then Two React Children");
    }
    switch (container.getChildCount()) {
      case 0:
        container.addView(child);
        break;
    }
  }

  protected void start() {
    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        // ARCore requires camera permissions to operate. If we did not yet obtain runtime
        // permission on Android M and above, now is a good time to ask the user for it.
        if (!CameraPermissionHelper.hasCameraPermission(reactContext.getCurrentActivity())) {
          CameraPermissionHelper.requestCameraPermission(this.reactContext.getCurrentActivity());
          return;
        }

        // Create the session and configure it to use a front-facing (selfie) camera.
        session = new Session(/* context= */ this.reactContext, EnumSet.noneOf(Session.Feature.class));
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
        configureSession();

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

      if (message != null) {
        messageSnackbarHelper.showError(this.reactContext.getCurrentActivity(), message);
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    // Note that order matters - see the note in onPause(), the reverse applies here.
    try {
      session.resume();
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(this.reactContext.getCurrentActivity(), "Camera not available. Try restarting the app.");
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

    // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
    try {
      // Create the texture and pass it to ARCore session to be filled during update().
      backgroundRenderer.createOnGlThread(/*context=*/ this.reactContext);
      augmentedFaceRenderer.createOnGlThread(this.reactContext, "models/freckles.png");
      augmentedFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
      noseObject.createOnGlThread(/*context=*/ this.reactContext, "models/nose.obj", "models/nose_fur.png");
      noseObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
      noseObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);
      rightEarObject.createOnGlThread(this.reactContext, "models/forehead_right.obj", "models/ear_fur.png");
      rightEarObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
      rightEarObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);
      leftEarObject.createOnGlThread(this.reactContext, "models/forehead_left.obj", "models/ear_fur.png");
      leftEarObject.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
      leftEarObject.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);

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
        augmentedFaceRenderer.draw(
          projectionMatrix, viewMatrix, modelMatrix, colorCorrectionRgba, face);

        // 2. Next, render the 3D objects attached to the forehead.
        face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT).toMatrix(rightEarMatrix, 0);
        rightEarObject.updateModelMatrix(rightEarMatrix, scaleFactor);
        rightEarObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR);

        face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT).toMatrix(leftEarMatrix, 0);
        leftEarObject.updateModelMatrix(leftEarMatrix, scaleFactor);
        leftEarObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR);

        // 3. Render the nose last so that it is not occluded by face mesh or by 3D objects attached
        // to the forehead regions.
        face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP).toMatrix(noseMatrix, 0);
        noseObject.updateModelMatrix(noseMatrix, scaleFactor);
        noseObject.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR);
      }
    } catch (Throwable t) {
      // Avoid crashing the application due to unhandled exceptions.
      Log.e(TAG, "Exception on the OpenGL thread", t);
    } finally {
      GLES20.glDepthMask(true);
    }
  }

  private void configureSession() {
    Config config = new Config(session);
    config.setAugmentedFaceMode(Config.AugmentedFaceMode.MESH3D);
    session.configure(config);
  }
}
