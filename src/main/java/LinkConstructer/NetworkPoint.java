package LinkConstructer;

import java.io.Serializable;
import java.util.List;

// Invariant
public class NetworkPoint implements Serializable {

  public static long PRECISION = 1000L;
  public static long EMPTY_TO = -1;

  public final long from;
  public final long to;
  private final long fraction; // from -> to 기준으로

  public NetworkPoint(long from, long to, double fraction) {
    this.from = from;
    this.to = to;
    this.fraction = (long) (fraction * PRECISION);
    assertValid();
  }

  public NetworkPoint(long from) {
    this(from, EMPTY_TO, 0);
  }

  void assertValid() {
    if (to < 0) {
      assert fraction == 0;
    }
    assert fraction >= 0 && fraction < PRECISION : toString();
    assert from >= 0;

    if (to < 0) {
      if (fraction > 0) {
        throw new RuntimeException(" fraction > 0: " + toString());
      }
    }
    if (fraction < 0 || fraction >= PRECISION) {
      throw new RuntimeException(toString());
    }

    if (from < 0) {
      throw new RuntimeException("from < 0");
    }
  }

  public boolean isSameLink(NetworkPoint that) {
    return this.from == that.from && this.to == that.to;
  }

  public boolean isReverseSameLink(NetworkPoint that) {
    return this.from == that.to && this.to == that.from;
  }

  @Override
  public String toString() {
    return "NetworkPoint{" +
        "from=" + from +
        ", to=" + to +
        ", fraction=" + getFraction() + " " + fraction +
        '}';
  }

  // modify self and return self
  public NetworkPoint reverse() {
    if (fraction > 0) {
      return new NetworkPoint(to, from, (PRECISION - getFractionInner()) * 1.0 / PRECISION);
    } else {
      return new NetworkPoint(from);
    }
  }

//  // nextNodes도 수정한다
//  public NetworkPoint pivot(List<Long> nextNodes){
//    assert fraction == 0;
//    assert from == nextNodes.get(0);
//    System.out.println(toString() + " " + nextNodes);
//    return new NetworkPoint(nextNodes.remove(0), nextNodes.get(0), getFraction());
//  }

  public NetworkPoint fix(long newTo) {
    assert fraction == 0;
    return new NetworkPoint(from, newTo, getFraction());
  }

  public boolean isAlign(List<Long> nodes) {
    for (int i = 0; i < nodes.size() - 1; i++) {
      if (from == nodes.get(i) && to == nodes.get(i + 1)) {
        return true;
      }
    }
    return false;
  }

  public NetworkPoint reverseUnlessAlign(List<Long> nodes) {
    if (fraction == 0) {
      int idx = indexOfNodes(nodes, this);
      if (idx >= 0 && idx < nodes.size() - 1) {
        return this.fix(nodes.get(idx + 1));
      }
      return this;
    }

    if (isAlign(nodes)) {
      return this;
    }
    if (nodes.contains(from) && nodes.contains(to)) {
      return reverse();
    } else {
      return this;
    }
  }

  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    NetworkPoint that = (NetworkPoint) obj;
    return this.from == that.from && this.to == that.to && this.fraction == that.fraction;
  }

  @Override
  public int hashCode() {
    int result = (int) (from ^ (from >>> 32));
    result = 31 * result + (int) (to ^ (to >>> 32));
    result = 31 * result + (int) (fraction ^ (fraction >>> 32));
    return result;
  }

  public boolean same(NetworkPoint that) {
    return equals(that)
        || (that.fraction > 0 && equals(that.reverse()))
        || (fraction == 0 && that.fraction == 0 && from == that.from);
  }

  public static int indexOfNodes(List<Long> nodes, NetworkPoint pt) {
    if (pt.fraction == 0) {
      for (int i = 0; i < nodes.size(); i++) {
        if (nodes.get(i).equals(pt.from)) {
          return i;
        }
      }
      return -1;
    }

    for (int i = 0; i < nodes.size() - 1; i++) {
      if (nodes.get(i).equals(pt.from) && nodes.get(i + 1).equals(pt.to)) {
        return i;
      }
    }
    return -1;
  }

  // from부터 fraction까지의 길이
  public double fromLength(Link link) {
    if (from == link.startNodeId && to == link.endNodeId) {
      return link.geometry.getLength() * getFraction();
    } else if (from == link.endNodeId && to == link.startNodeId) {
      return link.geometry.getLength() * (1 - getFraction());
    } else if (getFraction() == 0) {
      return 0;
    } else {
      throw new RuntimeException("not here");
    }
  }

  public boolean isFixed() {
    return this.to != EMPTY_TO;
  }

  public double getFraction() {
    return fraction * 1.0 / PRECISION;
  }

  public long getFractionInner() {
    return fraction;
  }

  // curr, next의 align을 고려하지 않는다. 연결성만 고려한다.
  // RoutingService에서 사용할 수 있다.
}
