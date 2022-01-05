package com.reactnativegooglearcore.augmentedfaces;

import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Pose;

public class AugmentedFaceRegions {
  public AugmentedFace face;
  public AugmentedFaceRegions(AugmentedFace face) {
    this.face = face;
  }

  public enum RegionType {
    /**
     * Lado direito da testa
     */
    FOREHEAD_RIGHT,
    /**
     * Lado esquerdo da testa
     */
    FOREHEAD_LEFT,
    /**
     * Centro da testa
     */
    FOREHEAD_CENTER,
    /**
     * Nariz
     */
    NOSE_TIP,
    /**
     * Olho esquerdo
     */
    EYE_LEFT,
    /**
     * Olho direito
     */
    EYE_RIGHT,
    /**
     * Bigode
     */
    MUSTACHE,
    /**
     * Cabelos
     */
    HAIR,
    /**
     * Queixo
     */
    CHIN,
    /**
     * Bochecha esquerda
     */
    CHEEK_LEFT,
    /**
     * Bochecha direita
     */
    CHEEK_RIGHT
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
      case HAIR:
        return getLandmarkPose(10);
      case FOREHEAD_CENTER:
        return getLandmarkPose(151);
      case CHIN:
        return getLandmarkPose(199);
      case CHEEK_RIGHT:
        return getLandmarkPose(352);
      case CHEEK_LEFT:
        return getLandmarkPose(123);
      default:
        return face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP);
    }
  }

  private Pose getLandmarkPose(int vertexIndex) {
    return AugmentedFaceLandmarks.getLandmarkPose(face, vertexIndex);
  }
}
