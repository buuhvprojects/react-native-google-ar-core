package com.reactnativegooglearcore.augmentedfaces;

import android.content.Context;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.effects.Object3D;

import java.util.ArrayList;

public interface AugmentedFaceInterface {
  AugmentedFace face = null;
  Context context = null;

  ArrayList<FaceRegion> landmarks = new ArrayList<>();
  ArrayList<FaceRegion> landmarksTexture = new ArrayList<>();

  ArrayList<Object3D> object3Ds = new ArrayList<>();

  boolean requireTexture();
  void createObjects();
  void draw(AugmentedFace face, float[] projectionMatrix, float[] viewMatrix, float[] colorCorrectionRgba);
  void drawTexture(AugmentedFace face, float[] projectionMatrix, float[] viewMatrix, float[] modelMatrix, float[] colorCorrectionRgba);
}
