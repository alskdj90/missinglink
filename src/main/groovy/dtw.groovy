import LinkConstructer.DTW
import LinkConstructer.NetworkService
import com.vividsolutions.jts.geom.Coordinate
import org.json.JSONObject
import org.json.JSONTokener
import LinkConstructer.Utils
import LinkConstructer.JSON
import LinkConstructer.NetworkFactory

File folder = new File("/Users/luke.alskdj90/WebstormProjects/map/navi_trip3")
String link =  new File("/Users/luke.alskdj90/Downloads/navi_links_20190306")
File out = new File("/Users/luke.alskdj90/Desktop/dtw_fixed2.txt")
FileWriter w = new FileWriter(out)
BufferedWriter writer = new BufferedWriter(w)
writer.write("filename,score\n")
NetworkService networkService = NetworkFactory.getNetworkServiceInstance(link)
File[] files = folder.listFiles(new FileFilter() {
  @Override
  boolean accept(File pathname) {
    return pathname.name.contains("json")
  }
})
JSONObject json
JSONTokener tokener
count = 1
List<Coordinate> gpsList, linkList
double value
FileInputStream inputStream
for (File file : files) {
  count++
  try {
    inputStream = new FileInputStream(file)
    tokener = new JSONTokener(inputStream)
    json = new JSONObject(tokener)
    gpsList = Utils.gpsToCoordinates(JSON.loadPositions(json.getJSONArray('gpsList')))
    linkList = Utils.matchedToCoordinates(JSON.loadComplexPoints(json.getJSONArray('matchedList')), networkService)
    DTW dtw = new DTW(gpsList, linkList)
    value = dtw.doit()
    writer.append(String.format("%s,%s\n", file.name, value))
    inputStream.close()
  } catch (Exception e) {
    e.printStackTrace()
  }
  if (count > 3000) break
}
writer.close()
w.close()


