package com.reactnativegooglearcore.augmentedfaces;

import android.content.Context;

import com.google.ar.core.AugmentedFace;

public interface AugmentedFaceInterface {
  AugmentedFace face = null;
  Context context = null;

  boolean requireTexture();
  void createObjects();
  void draw(AugmentedFace face, float[] projectionMatrix, float[] viewMatrix, float[] colorCorrectionRgba);
  void drawTexture(AugmentedFace face, float[] projectionMatrix, float[] viewMatrix, float[] modelMatrix, float[] colorCorrectionRgba);
}
