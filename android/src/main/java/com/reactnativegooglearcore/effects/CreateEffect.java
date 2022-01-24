package com.reactnativegooglearcore.effects;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceInterface;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;
import com.reactnativegooglearcore.augmentedfaces.FaceRegion;

import java.util.ArrayList;

public class CreateEffect implements AugmentedFaceInterface {
  Context context;
  ArrayList<FaceRegion> landmarks = new ArrayList<>();
  ArrayList<FaceRegion> landmarksTexture = new ArrayList<>();
  boolean devMode = false;

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
  public void cleanObjects() {
    landmarks = new ArrayList<>();
    landmarksTexture = new ArrayList<>();
  }

  @Override
  public void createObjects() {
    cleanObjects();
    for (Object3D object3D: object3Ds) {
      if (!object3D.object.isEmpty()) {
        landmarks.add(new FaceRegion(context, devMode));
        landmarks.get(landmarks.size() - 1)
          .create(
            object3D.object,
            object3D.texture,
            object3D.regionType
          );
      } else {
        landmarksTexture.add(new FaceRegion(context, devMode));
        landmarksTexture.get(landmarksTexture.size() - 1)
          .createMakeup(object3D.texture);
      }
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

  @Override
  public void setDevMode(boolean devMode) {
    this.devMode = devMode;
    for (FaceRegion landmark: landmarksTexture) {
      landmark.setDevMode(this.devMode);
    }
    for (FaceRegion landmark: landmarks) {
      landmark.setDevMode(this.devMode);
    }
  }
}
