package com.reactnativegooglearcore.common.helpers;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class SaveBitmapFile {
  public String save(Bitmap bitmap) {
    if (isExternalStorageWritable()) {
      return saveImage(bitmap);
    } else {
      return "";
    }
  }

  private String saveImage(Bitmap finalBitmap) {
    Log.d("SaveBitmapFile", "Salvando imagem");
    String root =  Environment.getExternalStoragePublicDirectory(
      Environment.DIRECTORY_PICTURES).toString();
    File myDir = new File(root);
    myDir.mkdirs();

    Random generator = new Random();
    int n = 10000;
    n = generator.nextInt(n);
    String fname = "Image-" + n + ".jpg";

    File file = new File(myDir, fname);
    if (file.exists()) file.delete();
    try {
      FileOutputStream out = new FileOutputStream(file);
      finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();
      Log.d("SaveBitmapFile", "Imagem salva");
      return file.toString();
    } catch (Exception e) {
      Log.d("SaveBitmapFile", "Falha ao salvar imagem: " + e.getMessage());
      e.printStackTrace();
    }
    return "";
  }

  /* Checks if external storage is available for read and write */
  public boolean isExternalStorageWritable() {
    Log.d("SaveBitmapFile", "Verificando permissão de escrita");
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      Log.d("SaveBitmapFile", "Permissão concedida");
      return true;
    }
    Log.d("SaveBitmapFile", "Permissão negada");
    return false;
  }
}
