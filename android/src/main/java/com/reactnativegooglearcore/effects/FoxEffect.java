package com.reactnativegooglearcore.effects;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceInterface;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;

import java.util.ArrayList;

public class FoxEffect implements AugmentedFaceInterface {
  Context context;
  ArrayList<FaceRegion> foxLandmarks = new ArrayList<>();
  public AugmentedFaceRegions face;

  public FoxEffect(Context context) {
    this.context = context;
  }

  @Override
  public void createObjects() {
    foxLandmarks.add(new FaceRegion(context));
    foxLandmarks.get(0)
      .create(
        "models/nose.obj",
        "models/nose_fur.png",
        AugmentedFaceRegions.RegionType.NOSE_TIP
      );
    foxLandmarks.add(new FaceRegion(context));
    foxLandmarks.get(1)
      .create(
        "models/forehead_left.obj",
        "models/ear_fur.png",
        AugmentedFaceRegions.RegionType.FOREHEAD_LEFT
      );
    foxLandmarks.add(new FaceRegion(context));
    foxLandmarks.get(2)
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
    GLES20.glDepthMask(false);

    foxLandmarks.get(0).update(
      face,
      projectionMatrix,
      viewMatrix,
      colorCorrectionRgba
    );
    foxLandmarks.get(1).update(
      face,
      projectionMatrix,
      viewMatrix,
      colorCorrectionRgba
    );
    foxLandmarks.get(2).update(
      face,
      projectionMatrix,
      viewMatrix,
      colorCorrectionRgba
    );
  }
}
