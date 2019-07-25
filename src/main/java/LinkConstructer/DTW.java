package LinkConstructer;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import java.util.List;

public class DTW {
  public List<Coordinate> gpsList;
  public List<Coordinate> linkList;

  public DTW(List<Coordinate> gpsList, List<Coordinate> linkList) {
    this.gpsList = gpsList;
    this.linkList = linkList;
  }



  public double doit() {
    int N = linkList.size(), M = gpsList.size() - 1;
    if(M == 1 || N == 0) {
      return 0;
    }
    double[][] score = new double[N][M];
    double min, index;
    Coordinate linkpoint;
    GeometryFactory factory = new GeometryFactory();
    LineString line;
    LengthIndexedLine indexedLine;
    for (int j = 0; j < M; j++) {
      line = factory.createLineString(new Coordinate[]{gpsList.get(j), gpsList.get(j + 1)});
      indexedLine = new LengthIndexedLine(line);
      index = indexedLine.project(linkList.get(0));
      score[0][j] = linkList.get(0).distance(indexedLine.extractPoint(index));
    }
    for (int i = 1; i < N; i++) {
      min = Double.MAX_VALUE;
      linkpoint = linkList.get(i);
      for (int j = 0; j < M; j++) {
        min = Math.min(min, score[i - 1][j]);
        line = factory.createLineString(new Coordinate[]{gpsList.get(j), gpsList.get(j + 1)});
        indexedLine = new LengthIndexedLine(line);
        index = indexedLine.project(linkpoint);
        score[i][j] = linkpoint.distance(indexedLine.extractPoint(index)) + min;
      }
    }
    return score[N - 1][M - 1] / N;
  }
}
