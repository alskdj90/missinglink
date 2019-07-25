package LinkConstructer;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// Invariant
public class ComplexPoint implements Serializable {

  private static final long serialVersionUID = 1L;

  public final long time;
  public final Coordinate coord;
  public final NetworkPoint networkPoint;
  public final List<Long> nodes;

  public ComplexPoint(long time, Coordinate coord, NetworkPoint networkPoint, List<Long> nodes) {
    this.time = time;
    this.coord = coord;

    if (nodes != null && nodes.size() > 0 && nodes.get(nodes.size() - 1).equals(networkPoint.to)) {
      if (networkPoint.getFraction() > 0) {
        this.networkPoint = networkPoint.reverse();
      } else {
        this.networkPoint = new NetworkPoint(networkPoint.from);
        nodes.add(networkPoint.from);
      }
    } else {
      this.networkPoint = networkPoint;
    }

    if (nodes != null) {
      this.nodes = nodes instanceof Serializable ? nodes
          : new ArrayList<>(nodes); // subList가 Serializable하지 않아 가끔 문제가 된다.
    } else {
      this.nodes = null;
    }
  }

  public ComplexPoint(long time, Coordinate coord, long from, Long to, double fraction,
                      List<Long> nodes) {
    this(time, coord, new NetworkPoint(from, to, fraction), nodes);
  }

  /*
  public void setNetworkPoint(NetworkPoint networkPoint){
    this.networkPoint = networkPoint;
    assertValid();
  }
  */
  public ComplexPoint set(NetworkPoint networkPt, List<Long> newNodes) {
    return new ComplexPoint(time, coord, networkPt, newNodes);
  }

  public ComplexPoint set(NetworkPoint networkPt) {
    return new ComplexPoint(time, coord, networkPt, nodes);
  }

  public ComplexPoint set(List<Long> newNodes) {
    return new ComplexPoint(time, coord, networkPoint, newNodes);
  }

  public ComplexPoint copyWithNextTick() {
    return new ComplexPoint(time + 1, coord, networkPoint,
        nodes != null ? new ArrayList<>(this.nodes) : null);
  }

  @Override
  public String toString() {
    return "ComplexPoint{" +
        "time=" + time +
        ", networkPoint=" + networkPoint +
        ", nodes=" + nodes +
        ", coord=" + coord +
        '}';
  }

  public boolean equals(Object obj) {
    ComplexPoint that = (ComplexPoint) obj;
    return this.time == that.time
        && this.coord.distance(that.coord) < 0.00001
        && this.networkPoint.equals(that.networkPoint)
        && this.nodes.equals(that.nodes);
  }

}
