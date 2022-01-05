package com.reactnativegooglearcore.effects;

import android.content.Context;
import android.opengl.GLES20;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceInterface;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;
import com.reactnativegooglearcore.augmentedfaces.FaceRegion;

import java.util.ArrayList;

public class FoxEffect implements AugmentedFaceInterface {
  Context context;
  ArrayList<FaceRegion> landmarks = new ArrayList<>();
  public AugmentedFaceRegions face;

  public FoxEffect(Context context) {
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
        "models/nose.obj",
        "models/nose_fur.png",
        AugmentedFaceRegions.RegionType.NOSE_TIP
      );
    landmarks.add(new FaceRegion(context));
    landmarks.get(1)
      .create(
        "models/forehead_left.obj",
        "models/ear_fur.png",
        AugmentedFaceRegions.RegionType.FOREHEAD_LEFT
      );
    landmarks.add(new FaceRegion(context));
    landmarks.get(2)
      .create(
        "models/forehead_right.obj",
        "models/ear_fur.png",
        AugmentedFaceRegions.RegionType.FOREHEAD_RIGHT
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
