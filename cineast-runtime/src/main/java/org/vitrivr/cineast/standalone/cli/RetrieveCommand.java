package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import java.util.List;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.ConstrainedQueryConfig;
import org.vitrivr.cineast.standalone.listener.RetrievalResultCSVExporter;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;


/**
 * A CLI command that can be used to retrieve data from the database based on an example segment.
 *
 * @author Ralph Gasser
 * @version 1.0
 */
@Command(name = "retrieve", description = "Retrieves objects from the database using an example object.")
public class RetrieveCommand implements Runnable {

  @Option(name = {"-s", "--segmentid"}, title = "Segment ID", description = "The ID of the segment to use an example for retrieval.")
  private String segmentId;

  @Option(name = {"-k", "--category"}, title = "Category", description = "Name of the feature category to retrieve.")
  private String category;

  @Option(name = {"-e", "--export"}, title = "Export", description = "Indicates whether the results should be exported. Defaults to false.")
  private boolean export = false;

  @Option(name = {"-r", "--relevantSegments"}, title = "Relevant segments", description = "Comma separated list of segment IDs to which the query is to be limited.")
  private String relevantSegments;

  public void run() {
    final ContinuousRetrievalLogic retrieval = new ContinuousRetrievalLogic(Config.sharedConfig().getDatabase());
    if (export) {
      retrieval.addRetrievalResultListener(new RetrievalResultCSVExporter(Config.sharedConfig().getDatabase()));
    }


    QueryConfig qc = QueryConfig.newQueryConfigFromOther(new ConstrainedQueryConfig());

    if (relevantSegments != null && !relevantSegments.isEmpty()) {
      String[] segments = relevantSegments.split(",");
      for (String segment : segments) {
        qc.addRelevantSegmentIds(segment);
      }
    }

    final List<SegmentScoreElement> results = retrieval.retrieve(this.segmentId, this.category, qc);
    System.out.println("results:");
    for (SegmentScoreElement e : results) {
      System.out.print(e.getSegmentId());
      System.out.print(": ");
      System.out.println(e.getScore());
    }
    System.out.println();
  }
}
