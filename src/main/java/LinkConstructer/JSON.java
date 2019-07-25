package LinkConstructer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.json.JSONArray;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;

public class JSON {
  public static List<Coordinate> loadCoordinates(JSONArray array) {
    List<Coordinate> list = new ArrayList<>(array.length());
    for (int i = 0; i < array.length(); i++) {
      JSONObject json = array.getJSONObject(i);
      list.add(loadCoordinate(json));
    }
    return list;
  }
  public static Coordinate loadCoordinate(JSONObject json) {
    return new Coordinate(json.getDouble("x"), json.getDouble("y"));
  }
  public static JSONArray toJSON(List<Coordinate> positions) {
    JSONArray array = new JSONArray();
    for(Coordinate coordinate : positions) {
      array.put(toJSON(coordinate));
    }
    return array;
  }
  public static JSONObject toJSON(Coordinate position) {
    JSONObject json = new JSONObject();
    double[] wgs = Utils.ktm2wgs(position);
    json.put("x", wgs[0]);
    json.put("y", wgs[1]);
    return json;
  }
  public static List<ComplexPoint> loadComplexPoints(JSONArray arr) {
    List<ComplexPoint> list = new ArrayList<>();
    for (int i = 0; i < arr.length(); i++) {
      list.add(loadComplexPoint(arr.getJSONObject(i)));
    }
    return list;
  }
  public static Position loadPosition(JSONObject json) {
    return new Position(
        json.getLong("time"),
        Utils.wgs2wtm(json.getDouble("x"), json.getDouble("y"))
    );
  }

  public static List<Position> loadPositions(JSONArray arr) {
    List<Position> list = new ArrayList<Position>();
    for (int i = 0; i < arr.length(); i++) {
      list.add(loadPosition(arr.getJSONObject(i)));
    }
    return list;
  }
  public static ComplexPoint loadComplexPoint(JSONObject json) {
    if (json.keySet().contains("_fraction")) {
      return new ComplexPoint(json.getLong("time"),
          new Coordinate(json.getDouble("x"), json.getDouble("y")),
          json.getLong("from"),
          json.getLong("to"),
          json.getDouble("_fraction"),
          loadNodeArr(json.getJSONArray("nodes")));
    } else {
      throw new RuntimeException("no _fraction");
    }
  }
  private static List<Long> loadNodeArr(JSONArray arr) {
    List<Long> result = new ArrayList<Long>();
    for (int i = 0; i < arr.length(); i++) {
      result.add(arr.getLong(i));
    }
    return result;
  }

}
