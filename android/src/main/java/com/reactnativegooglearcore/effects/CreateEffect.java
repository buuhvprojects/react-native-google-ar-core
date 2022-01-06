package com.reactnativegooglearcore.effects;

import android.content.Context;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceInterface;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;
import com.reactnativegooglearcore.augmentedfaces.FaceRegion;

import java.util.ArrayList;

public class CreateEffect implements AugmentedFaceInterface {
  Context context;
  ArrayList<FaceRegion> landmarks = new ArrayList<>();
  ArrayList<FaceRegion> landmarksTexture = new ArrayList<>();

  public AugmentedFaceRegions face;
  private ArrayList<Object3D> object3Ds;

  public CreateEffect(Context context, ArrayList<Object3D> object3Ds) {
    this.context = context;
    this.object3Ds = object3Ds;
  }

  @Override
  public boolean requireTexture() {
    return landmarksTexture.size() > 0 ? true : false;
  }

  @Override
  public void createObjects() {
    landmarks = new ArrayList<>();
    int i = 0;
    for (Object3D object3D: object3Ds) {
      if (!object3D.object.isEmpty()) {
        landmarks.add(new FaceRegion(context));
        landmarks.get(i)
          .create(
            object3D.object,
            object3D.texture,
            object3D.regionType
          );
      } else {
        landmarksTexture.add(new FaceRegion(context));
        landmarksTexture.get(i)
          .createMakeup(object3D.texture);
      }
      i++;
    }
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
  public void drawTexture(
    AugmentedFace face,
    float[] projectionMatrix,
    float[] viewMatrix,
    float[] modelMatrix,
    float[] colorCorrectionRgba
  ) {
    for (FaceRegion landmark: landmarksTexture) {
      landmark.updateTexture(
        face,
        projectionMatrix,
        viewMatrix,
        modelMatrix,
        colorCorrectionRgba
      );
    }
  }
}
