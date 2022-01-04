package com.reactnativegooglearcore;

import android.opengl.GLSurfaceView;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.reactnativegooglearcore.effects.CameraEffect;

public class GoogleArCoreViewManager extends ViewGroupManager<CoordinatorLayout> {
  public static final String REACT_CLASS = "GoogleArCoreView";
  private ReactApplicationContext reactContext;
  private CoordinatorLayout container;
  private CoordinatorLayout containerSurfaceview;
  private View view;

  private GLSurfaceView surfaceView;

  private CameraEffect cameraEffect;

  public GoogleArCoreViewManager(ReactApplicationContext reactContext) {
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  public CoordinatorLayout createViewInstance(ThemedReactContext context) {
    view = LayoutInflater.from(context).inflate(R.layout.main, null);
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
      startSession();
      return defaultView;
    }
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

  /**
   * Ativa o evento de capturar a imagem de camera que está sendo exibida na tela
   * @param promise
   */
  public void setRequestedCapture(Promise promise) {
    if (cameraEffect != null) {
      cameraEffect.capture();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  @ReactProp(name = "imagesDir")
  public void setImagesDir(View view, @Nullable String dir) {
    if (cameraEffect != null) {
      cameraEffect.setDir(dir);
    }
  }

  @ReactProp(name = "showNose")
  public void setDrawNose(View view, @Nullable boolean value) {
    if (cameraEffect != null) {
      cameraEffect.setDrawNose(value);
    }
  }

  @ReactProp(name = "showLeftEar")
  public void setDrawLeftEar(View view, @Nullable boolean value) {
    if (cameraEffect != null) {
      cameraEffect.setDrawLeftEar(value);
    }
  }

  @ReactProp(name = "showRightEar")
  public void setDrawRightEar(View view, @Nullable boolean value) {
    if (cameraEffect != null) {
      cameraEffect.setDrawRightEar(value);
    }
  }

  @ReactProp(name = "showFaceMakeup")
  public void setDrawFaceMakeup(View view, @Nullable boolean value) {
    if (cameraEffect != null) {
      cameraEffect.setDrawFaceMakeup(value);
    }
  }

  @ReactProp(name = "noseObj")
  public void setNoseObj(View view, @Nullable String value) {
    if (cameraEffect != null) {
      cameraEffect.setNoseObj(value);
    }
  }

  @ReactProp(name = "noseObjTexture")
  public void setNoseObjTexture(View view, @Nullable String value) {
    if (cameraEffect != null) {
      cameraEffect.setNoseObjTexture(value);
    }
  }

  @ReactProp(name = "leftEarObj")
  public void setLeftEarObj(View view, @Nullable String value) {
    if (cameraEffect != null) {
      cameraEffect.setLeftEarObj(value);
    }
  }

  @ReactProp(name = "leftEarObjTexture")
  public void setLeftEarObjTexture(View view, @Nullable String value) {
    if (cameraEffect != null) {
      cameraEffect.setLeftEarObjTexture(value);
    }
  }

  @ReactProp(name = "rightEarObj")
  public void setRightEarObj(View view, @Nullable String value) {
    if (cameraEffect != null) {
      cameraEffect.setRightEarObj(value);
    }
  }

  @ReactProp(name = "rightEarObjTexture")
  public void setRightEarObjTexture(View view, @Nullable String value) {
    if (cameraEffect != null) {
      cameraEffect.setRightEarObjTexture(value);
    }
  }

  @ReactProp(name = "faceMakeupTexture")
  public void setFaceMakeupTexture(View view, @Nullable String value) {
    if (cameraEffect != null) {
      cameraEffect.setFaceMakeupTexture(value);
    }
  }

  public void startRecording(Promise promise) {
    if (cameraEffect != null) {
      cameraEffect.startRecording(promise);
    } else {
      promise.resolve(false);
    }
  }

  public void stopRecording(Promise promise) {
    if (cameraEffect != null) {
      cameraEffect.stopRecording(promise);
    } else {
      promise.resolve(false);
    }
  }

  public void getRecordingStatus(Promise promise) {
    if (cameraEffect != null) {
      cameraEffect.getRecordingStatus(promise);
    } else {
      promise.resolve(false);
    }
  }

  private void startSession() {
    if (cameraEffect == null) {
      cameraEffect = new CameraEffect(reactContext, surfaceView);
      cameraEffect.start();
    }
  }

  public void pauseSession(Promise promise) {
    if (cameraEffect != null) {
      cameraEffect.pauseSession();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  public void resumeSession(Promise promise) {
    if (cameraEffect != null) {
      cameraEffect.resumeSession();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  public void stopSession(Promise promise) {
    if (cameraEffect != null) {
      cameraEffect.stopSession();
      cameraEffect = null;
    } else {
      promise.resolve(false);
    }
  }
}
