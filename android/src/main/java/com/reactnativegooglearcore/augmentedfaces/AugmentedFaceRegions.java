package com.reactnativegooglearcore.augmentedfaces;

import android.util.Log;

import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Pose;

public class AugmentedFaceRegions {
  public AugmentedFace face;
  public AugmentedFaceRegions(AugmentedFace face) {
    this.face = face;
  }

  public enum RegionType {
    FOREHEAD_RIGHT,
    FOREHEAD_LEFT,
    NOSE_TIP,
    EYE_LEFT,
    EYE_RIGHT,
    MUSTACHE
  }

  public Pose getRegionPose(AugmentedFaceRegions.RegionType regionType) {
    switch (regionType) {
      case FOREHEAD_LEFT:
        return face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_LEFT);
      case FOREHEAD_RIGHT:
        return face.getRegionPose(AugmentedFace.RegionType.FOREHEAD_RIGHT);
      case EYE_LEFT:
        return getLandmarkPose(374);
      case EYE_RIGHT:
        return getLandmarkPose(145);
      case MUSTACHE:
        return getLandmarkPose(11);
      default:
        return face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP);
    }
  }

  private Pose getLandmarkPose(int vertexIndex) {
    return AugmentedFaceLandmarks.getLandmarkPose(face, vertexIndex);
  }
}
