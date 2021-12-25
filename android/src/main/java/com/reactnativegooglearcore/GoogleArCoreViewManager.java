package com.reactnativegooglearcore;

import android.content.Intent;
import android.graphics.Color;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.annotations.ReactPropGroup;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFacesView;

import java.util.Map;

public class GoogleArCoreViewManager extends SimpleViewManager<AugmentedFacesView> {
    public static final String REACT_CLASS = "GoogleArCoreView";

    ReactApplicationContext reactContext;

    public GoogleArCoreViewManager(ReactApplicationContext reactContext) {
      this.reactContext = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    @NonNull
    public AugmentedFacesView createViewInstance(ThemedReactContext reactContext) {
      return new AugmentedFacesView(reactContext);
    }
}
