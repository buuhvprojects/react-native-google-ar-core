package com.reactnativegooglearcore.common.helpers;

import android.graphics.Bitmap;
import android.opengl.GLException;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

public class SaveBitmapFile {
  private Bitmap bitmap;
  private GL10 gl;
  private final int w;
  private final int h;
  public String filePath = "";
  public String errorMessage = "Cannot save image. Check logcat";
  private String DIRECTORY;

  public SaveBitmapFile(String DIRECTORY, GL10 gl, int w, int h) {
    this.DIRECTORY = DIRECTORY;
    this.gl = gl;
    this.w = w;
    this.h = h;
  }
  public void save() {
    if (isExternalStorageWritable()) {
      createBitmapFromGLSurface();
      saveImage();
    }
  }

  private void saveImage() {
    Log.d("SaveBitmapFile", "Salvando imagem");
    File myDir = new File(DIRECTORY);
    if (!myDir.exists()) {
      myDir.mkdirs();
    }

    Random generator = new Random();
    int n = 10000;
    n = generator.nextInt(n);
    String fname = "Image-" + n + ".jpg";

    File file = new File(myDir, fname);
    if (file.exists()) file.delete();
    try {
      FileOutputStream out = new FileOutputStream(file);
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.flush();
      out.close();
      Log.d("SaveBitmapFile", "Imagem salva");
      filePath = file.toString();
    } catch (Exception e) {
      Log.d("SaveBitmapFile", "Falha ao salvar imagem: " + e.getMessage());
      errorMessage = e.getMessage();
      e.printStackTrace();
    }
  }

  public boolean isExternalStorageWritable() {
    Log.d("SaveBitmapFile", "Verificando permissão de escrita");
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      Log.d("SaveBitmapFile", "Permissão concedida");
      return true;
    }
    Log.d("SaveBitmapFile", "Permissão negada");
    errorMessage = "It without write permission";
    return false;
  }

  private void createBitmapFromGLSurface()
    throws OutOfMemoryError {
    int bitmapBuffer[] = new int[w * h];
    int bitmapSource[] = new int[w * h];
    IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
    intBuffer.position(0);

    try {
      int x = 0;
      int y = 0;
      gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
      int offset1, offset2;
      for (int i = 0; i < h; i++) {
        offset1 = i * w;
        offset2 = (h - i - 1) * w;
        for (int j = 0; j < w; j++) {
          int texturePixel = bitmapBuffer[offset1 + j];
          int blue = (texturePixel >> 16) & 0xff;
          int red = (texturePixel << 16) & 0x00ff0000;
          int pixel = (texturePixel & 0xff00ff00) | red | blue;
          bitmapSource[offset2 + j] = pixel;
        }
      }
    } catch (GLException e) {
      errorMessage = e.getMessage();
      e.printStackTrace();
    }

    bitmap = Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
  }
}
