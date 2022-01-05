package com.reactnativegooglearcore.effects;

import android.content.Context;
import android.opengl.GLES20;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceInterface;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;
import com.reactnativegooglearcore.augmentedfaces.FaceRegion;

import java.util.ArrayList;

public class BeardEffect implements AugmentedFaceInterface {
  Context context;
  ArrayList<FaceRegion> landmarks = new ArrayList<>();
  public AugmentedFaceRegions face;

  public BeardEffect(Context context) {
    this.context = context;
  }

  @Override
  public boolean requireTexture() {
    return false;
  }

  @Override
  public void createObjects() {
    landmarks = new ArrayList<>();
    landmarks.add(new FaceRegion(context));
    landmarks.get(0)
      .create(
        "models/beard.obj",
        "models/beard.png",
        AugmentedFaceRegions.RegionType.MUSTACHE
      );
  }

  @Override
  public void draw(
    AugmentedFace face,
    float[] projectionMatrix,
    float[] viewMatrix,
    float[] colorCorrectionRgba
  ) {
    for (FaceRegion landmark: landmarks) {
      landmark.update(
        face,
        projectionMatrix,
        viewMatrix,
        colorCorrectionRgba
      );
    }
  }

  @Override
  public void drawTexture(AugmentedFace face, float[] projectionMatrix, float[] viewMatrix, float[] modelMatrix, float[] colorCorrectionRgba) {

  }
}
