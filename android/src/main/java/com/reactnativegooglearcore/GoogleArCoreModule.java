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

    @ReactMethod
    public void startRecording(Promise promise) {
      if (viewManager != null) {
       viewManager.startRecording(promise);
      } else {
        promise.resolve(false);
      }
    }

    @ReactMethod
    public void stopRecording(Promise promise) {
      if (viewManager != null) {
       viewManager.stopRecording(promise);
      } else {
        promise.resolve(false);
      }
    }

    @ReactMethod
    public void getRecordingStatus(Promise promise) {
      if (viewManager != null) {
       viewManager.getRecordingStatus(promise);
      } else {
        promise.resolve(false);
      }
    }

    @ReactMethod
    public void pauseSession(Promise promise) {
      if (viewManager != null) {
       viewManager.pauseSession(promise);
      } else {
        promise.resolve(false);
      }
    }

    @ReactMethod
    public void resumeSession(Promise promise) {
      if (viewManager != null) {
       viewManager.resumeSession(promise);
      } else {
        promise.resolve(false);
      }
    }

    @ReactMethod
    public void stopSession(Promise promise) {
      if (viewManager != null) {
       viewManager.stopSession(promise);
      } else {
        promise.resolve(false);
      }
    }

    public static native boolean capture();
}
