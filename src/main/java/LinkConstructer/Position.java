package LinkConstructer;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {

  private static final long serialVersionUID = 2310753222369380011L;

  public long time;
  public Coordinate coord;

  public Position(long time, Coordinate coord) {
    this.time = time;
    this.coord = coord;
  }

  @Override
  public String toString() {
    return "Position [time=" +time + " " + time +
        ", coord=" + coord + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Position position = (Position) o;
    return time == position.time &&
            coord.equals(position.coord);
  }

}
