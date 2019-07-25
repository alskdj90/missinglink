import LinkConstructer.ByteUnPacker

import LinkConstructer.JSON
import com.vividsolutions.jts.geom.Coordinate
import org.json.JSONObject
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

File file = new File("/Users/luke.alskdj90/Downloads/gps_logs.csv")
FileReader reader = new FileReader(file)
BufferedReader r = new BufferedReader(reader)
String line
String[] log
JSONObject json
r.readLine()
DescriptiveStatistics statistics = new DescriptiveStatistics()
List<Double> list = new ArrayList<>()
int count = 0
while ((line = r.readLine()) != null) {
  log = line.split(',')
  json = ByteUnPacker.unpackLog(log[1])
  List<Coordinate> pointList = JSON.loadCoordinates(json.getJSONArray('logs'))
  Coordinate prev = null
  for (Coordinate point : pointList) {
    if (prev != null) {
      statistics.addValue(prev.distance(point))
      list.add(prev.distance(point))
    }
    prev = point
  }
  count++
  if(count == 10000)
    break
}
println statistics
r.close()
reader.close()