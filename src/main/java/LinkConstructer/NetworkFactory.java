package LinkConstructer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class NetworkFactory {

  private static final Logger logger = LoggerFactory.getLogger(NetworkFactory.class);

  private static String FORWARD = "3";
  private static String BACKWARD = "4";

  private static NetworkService networkService;

  public static synchronized NetworkService getNetworkServiceInstance(String linkFileName) {
    if (networkService == null) {
      networkService = createNetworkService(linkFileName);
    }
    return networkService;
  }


  private static NetworkService createNetworkService(String fileName) {
    logger.info("load network from {}", fileName);    // TODO alpha system.log에 두번 출력된다
    List<Link> links = new ArrayList<>();

    try {
      Reader reader = null;
      if (fileName.endsWith(".gz")) {
        reader = new InputStreamReader(new GZIPInputStream(new FileInputStream(fileName)), "UTF-8");
      } else {
        reader = new FileReader(fileName);
      }

      BufferedReader r = new BufferedReader(reader);
      String line = r.readLine(); // header
      while ((line = r.readLine()) != null) {
        String[] arr = line.split("\\|");
        Link link = makeLink(arr);
        if (arr.length >= 5) {
          if (arr[4].equals(FORWARD)) {
            link.isForward = true;
          } else if (arr[4].equals(BACKWARD)) {
            link.isForward = false;
          }
        }
        if (arr.length >= 6) {
          link.category = Integer.parseInt(arr[5]);
        }
        if (arr.length >= 7) {
          link.lane = Integer.parseInt(arr[6]);
        }
        links.add(link);
      }
      r.close();
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    NetworkService service = new NetworkService(makeNetwork(links));
    return service;
  }

  private static Link makeLink(String[] arr) {
    Link link = new Link();
    link.id = Long.parseLong(arr[0]);
    link.startNodeId = Long.parseLong(arr[1]);
    link.endNodeId = Long.parseLong(arr[2]);
    try {
      link.geometry = (LineString) (new WKTReader().read(arr[3]));
    } catch (ParseException e) {
      System.out.println(arr[0] + " " + arr[3]);
      throw new RuntimeException(e);
    }
    return link;
  }


  public static Network makeNetwork(List<Link> links) {
    logger.info("links.size={}", links.size());

    Map<Long, Long> meshConnectionMap = makeMeshConnectionMap(links);

    Map<Long, Map<Long, Long>> g = new HashMap<>();
    Map<Long, Coordinate> nodeMap = new HashMap<>();
    Map<Long, Link> linkMap = new HashMap<>();

    for (Link link : links) {
      Long start =
          meshConnectionMap.containsKey(link.startNodeId) ? meshConnectionMap.get(link.startNodeId)
              : link.startNodeId;
      Long end =
          meshConnectionMap.containsKey(link.endNodeId) ? meshConnectionMap.get(link.endNodeId)
              : link.endNodeId;
      link.startNodeId = start;
      link.endNodeId = end;

      if (!g.containsKey(start)) {
          g.put(start, new HashMap<>());
        }
      g.get(start).put(end, link.id);
      if (!g.containsKey(end)) {
          g.put(end, new HashMap<>());
        }
      g.get(end).put(start, link.id);


      Coordinate[] coords = link.geometry.getCoordinates();
      nodeMap.put(start, coords[0]);
      nodeMap.put(end, coords[coords.length - 1]);
      linkMap.put(link.id, link);
    }

    return new Network(g, linkMap, nodeMap);
  }

  // 도엽이 연결되는 지점에서 대체될 Map<nodeId,nodeId>를 반환한다.
  private static Map<Long, Long> makeMeshConnectionMap(List<Link> links) {
    Map<Long, Long> exception = new HashMap<>();
    Map<String, Long> coordMap = new HashMap<>();
    for (Link link : links) {
      Coordinate[] coords = link.geometry.getCoordinates();
      Coordinate s = coords[0];
      String skey = String.format("%d_%d", (int) (s.x * 100), (int) (s.y * 100));
      if (coordMap.containsKey(skey)) {
        if (coordMap.get(skey) > link.startNodeId) {
          exception.put(coordMap.get(skey), link.startNodeId);
        } else if (coordMap.get(skey) < link.startNodeId) {
          exception.put(link.startNodeId, coordMap.get(skey));
        }
      } else {
        coordMap.put(skey, link.startNodeId);
      }

      Coordinate e = coords[coords.length - 1];
      String ekey = String.format("%d_%d", (int) (e.x * 100), (int) (e.y * 100));
      if (coordMap.containsKey(ekey)) {
        if (coordMap.get(ekey) > link.endNodeId) {
          exception.put(coordMap.get(ekey), link.endNodeId);
        } else if (coordMap.get(ekey) < link.endNodeId) {
          exception.put(link.endNodeId, coordMap.get(ekey));
        }
      } else {
        coordMap.put(ekey, link.endNodeId);
      }
    }

    return exception;
  }


}