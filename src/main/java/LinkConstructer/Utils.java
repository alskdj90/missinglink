package LinkConstructer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;
import net.daum.local.coord.converter.CoordConverter;
import net.daum.local.coord.converter.CoordSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
  static CoordConverter converter = new CoordConverter();
  public static Coordinate wgs2wtm(double x, double y) {
    double[] coord = converter.convert(x, y, CoordSystem.WGS84, CoordSystem.WTM);
    return new Coordinate(coord[0], coord[1]);
  }
  static double[] ktm2wgs(Coordinate ktm) {
    double[] wgs = converter.convert(ktm.x, ktm.y, CoordSystem.KTM, CoordSystem.WGS84);
    return wgs;
  }

  public static double heading(Coordinate prev, Coordinate curr) {
    double heading = Math.atan2(curr.y - prev.y, curr.x - prev.x) * 180 / Math.PI;
    heading = 90 - heading; // north:0, east:90
    if (heading < 0) {
      heading += 360;
    }
    return heading;
  }

  public static List<Coordinate> gpsToCoordinates(List<Position> gpsList) {
    return gpsList.stream().map(i -> i.coord).collect(Collectors.toList());
  }

  public static List<Coordinate> matchedToCoordinates(List<ComplexPoint> matchedList, NetworkService network) {
    List<LineString> links = new ArrayList<>();
    List<Coordinate> linkList = new ArrayList<>();
    Link link = null, prev = null;
    LineString line;
    for (ComplexPoint matched : matchedList) {
      if (!matched.nodes.isEmpty()) {
        List<Long> nodes = matched.nodes;
        for (int i = 0; i < nodes.size() - 1; i++) {
          link = network.getLink(nodes.get(i), nodes.get(i + 1));
          line = link.startNodeId.equals(nodes.get(i)) ? link.geometry : (LineString) link.geometry.reverse();
          links.add(line);
          prev = link;
        }
      }
      if (matched.networkPoint.to != -1) {
        link = network.getLink(matched.networkPoint.from, matched.networkPoint.to);
        if (!link.equals(prev)) {
          line = link.startNodeId.equals(matched.networkPoint.from) ? link.geometry : (LineString) link.geometry.reverse();
          links.add(line);
          prev = link;
        }
      }
    }
    int samplerate = 5;
    double rest = 0, length, startindex, endindex;
    LengthIndexedLine indexedLine;
    for (int i = 0; i < links.size(); i++) {
      indexedLine = new LengthIndexedLine(links.get(i));
      startindex = (i == 0) ? indexedLine.project(wgs2wtm(matchedList.get(0).coord.x, matchedList.get(0).coord.y)) : indexedLine.getStartIndex() + 5 - rest;
      endindex = (i == links.size() - 1) ?
          indexedLine.project(wgs2wtm(matchedList.get(matchedList.size() - 1).coord.x, matchedList.get(matchedList.size() - 1).coord.y)) : indexedLine.getEndIndex();
      length = endindex - startindex;
      for (int j = 0; j < (int)length / samplerate; j++) {
        linkList.add(indexedLine.extractPoint(startindex + samplerate * j));
      }
      rest = length - 5 * ((int) (length / samplerate));
    }
    return linkList;
  }
}
