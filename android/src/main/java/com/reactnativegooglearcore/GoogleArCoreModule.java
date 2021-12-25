package com.reactnativegooglearcore;

import android.app.Activity;

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

    public GoogleArCoreModule(ReactApplicationContext reactContext) {
      super(reactContext);
      context = reactContext;
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void createSession(Promise promise) {
        promise.resolve(true);
    }

    @ReactMethod
    public void trackAugmentedFaces(Promise promise) {
      promise.resolve(true);
    }

    public static native boolean createSession();
    public static native boolean trackAugmentedFaces();
}
