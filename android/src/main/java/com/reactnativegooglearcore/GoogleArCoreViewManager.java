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
import com.reactnativegooglearcore.effects.EffectRender;

public class GoogleArCoreViewManager extends ViewGroupManager<CoordinatorLayout> {
  public static final String REACT_CLASS = "GoogleArCoreView";
  private ReactApplicationContext reactContext;
  private CoordinatorLayout container;
  private CoordinatorLayout containerSurfaceview;
  private View view;

  private GLSurfaceView surfaceView;

  private EffectRender effectRender;

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
    if (effectRender != null) {
      effectRender.capture();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  @ReactProp(name = "imagesDir")
  public void setImagesDir(View view, @Nullable String dir) {
    if (effectRender != null) {
      effectRender.setDir(dir);
    }
  }

  @ReactProp(name = "effectIndex")
  public void setEffectIndex(View view, @Nullable int value) {
    if (effectRender != null) {
      effectRender.setEffectIndex(value);
    }
  }

  public void startRecording(Promise promise) {
    if (effectRender != null) {
      effectRender.startRecording(promise);
    } else {
      promise.resolve(false);
    }
  }

  public void stopRecording(Promise promise) {
    if (effectRender != null) {
      effectRender.stopRecording(promise);
    } else {
      promise.resolve(false);
    }
  }

  public void getRecordingStatus(Promise promise) {
    if (effectRender != null) {
      effectRender.getRecordingStatus(promise);
    } else {
      promise.resolve(false);
    }
  }

  private void startSession() {
    if (effectRender == null) {
      effectRender = new EffectRender(reactContext, surfaceView);
      effectRender.start();
    }
  }

  public void pauseSession(Promise promise) {
    if (effectRender != null) {
      effectRender.pauseSession();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  public void resumeSession(Promise promise) {
    if (effectRender != null) {
      effectRender.resumeSession();
      promise.resolve(true);
    } else {
      promise.resolve(false);
    }
  }

  public void stopSession(Promise promise) {
    if (effectRender != null) {
      effectRender.stopSession();
      effectRender = null;
    } else {
      promise.resolve(false);
    }
  }
}
