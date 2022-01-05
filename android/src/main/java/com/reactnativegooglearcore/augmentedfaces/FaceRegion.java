package com.reactnativegooglearcore.augmentedfaces;

import android.content.Context;
import android.util.Log;

import com.google.ar.core.AugmentedFace;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRegions;
import com.reactnativegooglearcore.augmentedfaces.AugmentedFaceRenderer;
import com.reactnativegooglearcore.common.rendering.ObjectRenderer;

import java.io.IOException;

public class FaceRegion {
  private ObjectRenderer objectRenderer = new ObjectRenderer();
  public final AugmentedFaceRenderer augmentedFaceRenderer = new AugmentedFaceRenderer();
  public Context context;
  public final float[] objectMatrix = new float[16];
  float scaleFactor = 1.0f;
  public static final float[] DEFAULT_COLOR = new float[] {0f, 0f, 0f, 0f};
  public AugmentedFaceRegions.RegionType regionType;

  public FaceRegion(Context context) {
    this.context = context;
  }

  public void create(String object3d, String objectTexture, AugmentedFaceRegions.RegionType regionType) {
    try {
      this.regionType = regionType;
      objectRenderer.createOnGlThread(context, object3d, objectTexture);
      objectRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
      objectRenderer.setBlendMode(ObjectRenderer.BlendMode.AlphaBlending);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void createMakeup(String texture) {
    try {
      augmentedFaceRenderer.createOnGlThread(context, texture);
      augmentedFaceRenderer.setMaterialProperties(0.0f, 1.0f, 0.1f, 6.0f);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void update(AugmentedFace face, float[] projectionMatrix, float[] viewMatrix, float[] colorCorrectionRgba) {
    AugmentedFaceRegions faceRegions = new AugmentedFaceRegions(face);
    faceRegions.getRegionPose(regionType).toMatrix(objectMatrix, 0);
    objectRenderer.updateModelMatrix(objectMatrix, scaleFactor);
    objectRenderer.draw(viewMatrix, projectionMatrix, colorCorrectionRgba, DEFAULT_COLOR);
  }
}
