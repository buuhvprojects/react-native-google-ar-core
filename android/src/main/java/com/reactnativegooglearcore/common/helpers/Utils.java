package com.reactnativegooglearcore.common.helpers;

import android.content.Context;

import java.io.File;
import java.io.FileWriter;

public class Utils {
  public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody, String folder){
    File dir = new File(mcoContext.getFilesDir(), folder);
    if(!dir.exists()){
      dir.mkdir();
    }

    try {
      File gpxfile = new File(dir, sFileName);
      FileWriter writer = new FileWriter(gpxfile);
      writer.append(sBody);
      writer.flush();
      writer.close();
    } catch (Exception e){
      e.printStackTrace();
    }
  }
}
