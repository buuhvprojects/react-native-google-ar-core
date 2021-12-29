package com.reactnativegooglearcore;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.List;

public class GoogleArCorePackage implements ReactPackage {
  private GoogleArCoreViewManager viewManager;
    @NonNull
    @Override
    public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();

        createViewManagerRef(reactContext);

        modules.add(new GoogleArCoreModule(reactContext, viewManager));
        return modules;
    }

    @NonNull
    @Override
    public List<ViewManager> createViewManagers(@NonNull ReactApplicationContext reactContext) {
        List<ViewManager> views = new ArrayList<>();

        createViewManagerRef(reactContext);

        views.add(viewManager);
        return views;
    }
    private void createViewManagerRef(ReactApplicationContext reactContext) {
      if (viewManager == null) {
        viewManager = new GoogleArCoreViewManager(reactContext);
      }
    }
}
