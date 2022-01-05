package com.reactnativegooglearcore.augmentedfaces;

import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Pose;

import java.nio.FloatBuffer;

public final class AugmentedFaceLandmarks {
  public static final Pose getLandmarkPose(AugmentedFace augmentedFace, int vertexIndex) {
    Pose centerPose = augmentedFace.getCenterPose();
    FloatBuffer buffer = augmentedFace.getMeshVertices();
    Pose pose;
    if (buffer != null) {
      pose = centerPose != null ? centerPose.compose(Pose.makeTranslation(buffer.get(vertexIndex * 3), buffer.get(vertexIndex * 3 + 1), buffer.get(vertexIndex * 3 + 2))) : null;
    } else {
      pose = null;
    }

    return pose;
  }
}
