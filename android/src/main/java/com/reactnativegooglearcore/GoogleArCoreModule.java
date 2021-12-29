package com.reactnativegooglearcore;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = GoogleArCoreModule.NAME)
public class GoogleArCoreModule extends ReactContextBaseJavaModule {
    public static final String NAME = "GoogleArCore";

    public ReactApplicationContext context;
    private GoogleArCoreViewManager viewManager;

    public GoogleArCoreModule(ReactApplicationContext reactContext, GoogleArCoreViewManager viewInstance) {
      super(reactContext);
      context = reactContext;
      if (viewInstance != null) {
        viewManager = viewInstance;
      }
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void capture(Promise promise) {
      if (viewManager != null) {
       viewManager.setRequestedCapture(promise);
      } else {
        promise.resolve(false);
      }
    }

    public static native boolean capture();
}
