package LinkConstructer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import java.io.Serializable;

public class Link implements Serializable {

  public Long id;
  public Long startNodeId;
  public Long endNodeId;
  public LineString geometry;
  public Boolean isForward; // null은 양방향, true는 startNodeId->endNodeId로만 허락됨
  public Integer category;
  public double width;
  public int lane;

  public Link() {
  }

  public Link(Long id, Long startNodeId, Long endNodeId, LineString geometry) {
    this.id = id;
    this.startNodeId = startNodeId;
    this.endNodeId = endNodeId;
    this.geometry = geometry;
  }

  // fromNode -> toNode로 이동했을때, isForward에 의도된 방향에 반대로 이동했는지를 판단.
  public boolean isReverse(Long fromNode, Long toNode) {
    if (isForward == null) {
      return false;
    }
    if (isForward) {
      return startNodeId.equals(toNode) || endNodeId.equals(fromNode);
    } else {
      return startNodeId.equals(fromNode) || endNodeId.equals(toNode);
    }
  }

  public boolean isReverse(double startFraction, double endFraction) {
    if (isForward == null) {
      return false;
    }
    if (isForward) {
      return startFraction > endFraction;
    } else {
      return startFraction < endFraction;
    }
  }

  public double getReverseLength(Long fromNode, Long toNode, double fraction) {
    if (isForward == null) {
      return 0;
    }
    if (isForward) {
      if (startNodeId.equals(toNode)) {
        return geometry.getLength() * fraction;
      } else if (endNodeId.equals(fromNode)) {
        return geometry.getLength() * (1 - fraction);
      }
    } else {
      if (startNodeId.equals(fromNode)) {
        return geometry.getLength() * fraction;
      } else if (endNodeId.equals(toNode)) {
        return geometry.getLength() * (1 - fraction);
      }
    }
    return 0;
  }

  public double getReverseLength(double startFraction, double endFraction) {
    if (isForward == null) {
      return 0;
    }
    if (isForward) {
      if (startFraction > endFraction) {
        return (startFraction - endFraction) * geometry.getLength();
      }
    } else {
      if (startFraction < endFraction) {
        return (endFraction - startFraction) * geometry.getLength();
      }
    }
    return 0;
  }

  public LineString subLine(double startFraction, double endFraction) {
    LengthIndexedLine indexedLine = new LengthIndexedLine(geometry);
    double len = geometry.getLength();
    return (LineString) indexedLine.extractLine(len * startFraction, len * endFraction);
  }

  public Coordinate extractPoint(double fraction) {
    LengthIndexedLine indexedLine = new LengthIndexedLine(geometry);
    return indexedLine.extractPoint(fraction * geometry.getLength());
  }

  public boolean isStartEnd(Long node1, Long node2) {
    return (this.startNodeId.equals(node1) && this.endNodeId.equals(node2));
  }

  public boolean isEndStart(Long node1, Long node2) {
    return (this.startNodeId.equals(node2) && this.endNodeId.equals(node1));
  }

  public boolean isBig() {
    if (category == null) {
      return false;
    }
    return category <= 1;
  }

  public boolean isMiddle() {
    if (category == null) {
      return false;
    }
    return category >= 2 && category <= 6 && lane >= 2;
  }

  public double getDirection(NetworkPoint networkPoint) {
    double direction;
    LengthIndexedLine indexedLine = new LengthIndexedLine(geometry);
    double norm = 0.1;
    double index = geometry.getLength() * networkPoint.getFraction();
    Coordinate projected = indexedLine.extractPoint(index);
    if(isStartEnd(networkPoint.from, networkPoint.to)) {
      if (index != indexedLine.getEndIndex()) {
        Coordinate next = indexedLine.extractPoint(indexedLine.indexOfAfter(projected, index + norm));
        direction = Utils.heading(projected, next);
      } else {
        Coordinate prev = indexedLine.extractPoint(index - norm);
        direction = Utils.heading(prev, projected);
      }
    } else {
      if (index != indexedLine.getStartIndex()) {
        Coordinate next = indexedLine.extractPoint(index - norm);
        direction = Utils.heading(projected, next);
      } else {
        Coordinate prev = indexedLine.extractPoint(indexedLine.indexOfAfter(projected, index + norm));
        direction = Utils.heading(prev, projected);
      }
    }
    return direction;
  }
  @Override
  public String toString() {
    return "Link [id=" + id + ", startNodeId=" + startNodeId
        + ", cate=" + category
        + ", endNodeId=" + endNodeId + ", geometry=" + geometry + "]";
  }
}
