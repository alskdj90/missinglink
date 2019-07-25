package LinkConstructer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.index.strtree.STRtree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Network {

  private static final Logger logger = LoggerFactory.getLogger(Network.class);

  public Map<Long, Map<Long, Long>> g = new HashMap<>();
  public Map<Long, Coordinate> nodes;
  public Map<Long, Link> links = new HashMap<>();
  protected STRtree linkTree = new STRtree();

  public Network(Map<Long, Map<Long, Long>> g, Map<Long, Link> links, Map<Long, Coordinate> nodes) {
    this.g = g;
    this.nodes = nodes;
    this.links = links;
    fillTree();
  }

  private void fillTree() {
    for (Link link : links.values()) {
      linkTree.insert(link.geometry.getEnvelopeInternal(), link);
    }
  }

  public Link getLink(long linkId) {
    return links.get(linkId);
  }

  public Long getLinkId(long startNodeId, long endNodeId) {
    if (endNodeId >= 0) {
      if (!g.containsKey(startNodeId)) {
        return null; // TODO
      }
      return g.get(startNodeId).get(endNodeId);
    } else if (startNodeId >= 0 && g.containsKey(startNodeId)) {
      for (Long newEnd : g.get(startNodeId).keySet()) {
        return g.get(startNodeId).get(newEnd);
      }
      return null;
    } else {
      return null;
    }
  }

  public Link getLink(long startNodeId, long endNodeId) {
    Long linkId = getLinkId(startNodeId, endNodeId);
    if (linkId != null) {
      return getLink(linkId);
    } else {
      return null;
    }
  }

}
