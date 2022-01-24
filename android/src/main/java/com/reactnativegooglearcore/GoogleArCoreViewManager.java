package com.reactnativegooglearcore;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.annotations.ReactPropGroup;
import com.reactnativegooglearcore.augmentedfaces.AugmentedEffectRender;

public class GoogleArCoreViewManager extends ViewGroupManager<CoordinatorLayout> {
  public static final String REACT_CLASS = "GoogleArCoreView";
  private ReactApplicationContext reactContext;
  private CoordinatorLayout container;
  private CoordinatorLayout containerSurfaceview;
  private View view;

  private GLSurfaceView surfaceView;

  private AugmentedEffectRender augmentedEffectRender;

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
    if (augmentedEffectRender != null) {
      augmentedEffectRender.capture();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  @ReactProp(name = "imagesDir")
  public void setImagesDir(View view, @Nullable String dir) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.setDir(dir);
    }
  }

  @ReactProp(name = "effectKey")
  public void setEffect(View view, @Nullable String value) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.setEffect(value);
    }
  }

  @ReactProp(name = "devMode")
  public void setDevMode(View view, @Nullable boolean value) {
    if (augmentedEffectRender != null){
      augmentedEffectRender.setDevMode(value);
    }
  }

  @ReactProp(name = "effects")
  public void setEffects(View view, @Nullable String effects) {
    if (augmentedEffectRender != null){
      augmentedEffectRender.setEffects(effects);
    }
  }

  public void startRecording(Promise promise) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.startRecording(promise);
    } else {
      promise.resolve(false);
    }
  }

  public void stopRecording(Promise promise) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.stopRecording(promise);
    } else {
      promise.resolve(false);
    }
  }

  public void getRecordingStatus(Promise promise) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.getRecordingStatus(promise);
    } else {
      promise.resolve(false);
    }
  }

  private void startSession() {
    if (augmentedEffectRender == null) {
      augmentedEffectRender = new AugmentedEffectRender(reactContext, surfaceView);
      augmentedEffectRender.start();
    }
  }

  public void pauseSession(Promise promise) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.pauseSession();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  public void resumeSession(Promise promise) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.resumeSession();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  public void stopSession(Promise promise) {
    if (augmentedEffectRender != null) {
      augmentedEffectRender.stopSession();
      augmentedEffectRender = null;
    } else {
      promise.resolve(false);
    }
  }
}
