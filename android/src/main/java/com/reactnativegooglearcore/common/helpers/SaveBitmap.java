package com.reactnativegooglearcore.common.helpers;

import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import javax.microedition.khronos.opengles.GL10;

public class SaveBitmap {
  private GLSurfaceView surfaceView;
  private ReactApplicationContext reactContext;
  private String DIRECTORY = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

  public void setDIRECTORY(String dir) {
    Log.d("setImagesDir", dir);
    DIRECTORY += "/" + dir;
  }
  public void setSurfaceView(GLSurfaceView view) {
    surfaceView = view;
  }
  public void setReactContext(ReactApplicationContext context) {
    reactContext = context;
  }
  public void onCaptureResult(String filePath) {
    if (reactContext != null) {
      Log.d("onCaptureResult", "Step 1");
      WritableMap eventData = Arguments.createMap();
      eventData.putString("filePath", filePath);
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("onChange", eventData);
    } else {
      Log.d("onCaptureResult", "Step 1 falhou com reactContext IS NULL");
    }
  }
  public void onFailedCapture(String errorMessage) {
    if (reactContext != null) {
      Log.d("onFailedCapture", "Step 1");
      WritableMap eventData = Arguments.createMap();
      eventData.putString("message", errorMessage);
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit("onFailedCapture", eventData);
    } else {
      Log.d("onFailedCapture", "Step 1 falhou com reactContext IS NULL");
    }
  }

  /**
   * Salva a imagem de camera que está sendo exibida na tela
   * @param gl
   */
  public void save(GL10 gl) {
    SaveBitmapFile saveBitmapFile = new SaveBitmapFile(
      DIRECTORY,
      gl,
      surfaceView.getWidth(),
      surfaceView.getHeight()
    );
    saveBitmapFile.save();
    String filePath = saveBitmapFile.filePath;
    if (!filePath.isEmpty()) {
      onCaptureResult(filePath);
    } else {
      onFailedCapture(saveBitmapFile.errorMessage);
    }
  }
}
