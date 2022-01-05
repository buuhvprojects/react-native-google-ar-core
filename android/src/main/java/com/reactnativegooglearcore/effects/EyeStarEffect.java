package com.reactnativegooglearcore.effects;

import android.content.Context;
import android.opengl.GLES20;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceInterface;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;
import com.reactnativegooglearcore.augmentedfaces.FaceRegion;

import java.util.ArrayList;

public class EyeStarEffect implements AugmentedFaceInterface {
  Context context;
  ArrayList<FaceRegion> landmarks = new ArrayList<>();
  public AugmentedFaceRegions face;

  public EyeStarEffect(Context context) {
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
        "models/star.obj",
        "models/star_texture.png",
        AugmentedFaceRegions.RegionType.EYE_LEFT
      );
    landmarks.add(new FaceRegion(context));
    landmarks.get(1)
      .create(
        "models/star.obj",
        "models/star_texture.png",
        AugmentedFaceRegions.RegionType.EYE_RIGHT
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
