package org.vitrivr.cineast.core.features;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.descriptor.EdgeImg;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.GridPartitioner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EdgeGrid16Full extends AbstractFeatureModule {

  private static final Logger LOGGER = LogManager.getLogger();

  public EdgeGrid16Full() {
    super("features_EdgeGrid16Full", 124f / 4f, 256);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    if (shot.getMostRepresentativeFrame() == VideoFrame.EMPTY_VIDEO_FRAME) {
      return;
    }
    if (!phandler.idExists(shot.getId())) {
      SummaryStatistics[] stats = new SummaryStatistics[256];
      for (int i = 0; i < 256; ++i) {
        stats[i] = new SummaryStatistics();
      }
      List<VideoFrame> videoFrames = shot.getVideoFrames();
      List<Boolean> edgePixels = new ArrayList<>();
      for (VideoFrame f : videoFrames) {
        MultiImage img = f.getImage();
        edgePixels = EdgeImg.getEdgePixels(img, edgePixels);
        ArrayList<LinkedList<Boolean>> partition = GridPartitioner.partition(edgePixels,
            img.getWidth(), img.getHeight(), 16, 16);
        for (int i = 0; i < partition.size(); ++i) {
          LinkedList<Boolean> edge = partition.get(i);
          SummaryStatistics stat = stats[i];
          for (boolean b : edge) {
            stat.addValue(b ? 1 : 0);
          }
        }
      }
      float[] result = new float[64];
      for (int i = 0; i < 64; ++i) {
        result[i] = (float) stats[i].getMean();
      }
      persist(shot.getId(), new FloatVectorImpl(result));
    }
  }

  private static FloatVector getEdges(MultiImage img) {
    SummaryStatistics[] stats = new SummaryStatistics[256];
    for (int i = 0; i < 256; ++i) {
      stats[i] = new SummaryStatistics();
    }
    List<Boolean> edgePixels = EdgeImg.getEdgePixels(img,
        new ArrayList<Boolean>(img.getWidth() * img.getHeight()));
    ArrayList<LinkedList<Boolean>> partition = GridPartitioner.partition(edgePixels, img.getWidth(),
        img.getHeight(), 16, 16);
    for (int i = 0; i < partition.size(); ++i) {
      LinkedList<Boolean> edge = partition.get(i);
      SummaryStatistics stat = stats[i];
      for (boolean b : edge) {
        stat.addValue(b ? 1 : 0);
      }
    }
    float[] f = new float[256];
    for (int i = 0; i < 256; ++i) {
      f[i] = (float) stats[i].getMean();
    }

    return new FloatVectorImpl(f);
  }

  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    FloatVector query = getEdges(sc.getMostRepresentativeFrame().getImage());
    return getSimilar(ReadableFloatVector.toArray(query), qc);
  }

}
