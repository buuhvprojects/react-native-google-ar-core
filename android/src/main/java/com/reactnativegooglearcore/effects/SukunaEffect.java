package com.reactnativegooglearcore.effects;

import android.content.Context;
import android.opengl.GLES20;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceInterface;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;
import com.reactnativegooglearcore.augmentedfaces.FaceRegion;

import java.util.ArrayList;

public class SukunaEffect implements AugmentedFaceInterface {
  Context context;
  ArrayList<FaceRegion> landmarks = new ArrayList<>();
  public AugmentedFaceRegions face;

  public SukunaEffect(Context context) {
    this.context = context;
  }

  @Override
  public void createObjects() {
    landmarks = new ArrayList<>();
    landmarks.add(new FaceRegion(context));
    landmarks.get(0)
      .createMakeup("models/sukuna_texture.png");
  }

  @Override
  public void drawTexture(
    AugmentedFace face,
    float[] projectionMatrix,
    float[] viewMatrix,
    float[] modelMatrix,
    float[] colorCorrectionRgba
  ) {
    for (FaceRegion landmark: landmarks) {
      landmark.updateTexture(
        face,
        projectionMatrix,
        viewMatrix,
        modelMatrix,
        colorCorrectionRgba
      );
    }
  }

  @Override
  public void draw(
    AugmentedFace face,
    float[] projectionMatrix,
    float[] viewMatrix,
    float[] colorCorrectionRgba
  ) {}

  @Override
  public boolean requireTexture() {
    return true;
  }
}
