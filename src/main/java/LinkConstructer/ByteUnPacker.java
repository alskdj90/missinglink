package LinkConstructer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Base64;

public class ByteUnPacker {
  public static JSONObject unpackLog(String log) {
    byte[] logbyte = Base64.getDecoder().decode(log);
    int index = 0;
    JSONObject json = new JSONObject();
    int log_format_version = (int)logbyte[index++];
    json.put("log_format_version", log_format_version);
    int trans_id_len = (int)logbyte[index++];
    String trans_id = "";

    for (;index < trans_id_len + 2;) {
      trans_id += (char)logbyte[index++];
    }
    json.put("trans_id", trans_id);
    int sdk_version = ByteToInt(logbyte, index);
    index += 4;
    json.put("sdk_version", sdk_version);
    long start_time, end_time;
    start_time = ByteToLong(logbyte, index);
    index += 8;
    end_time = ByteToLong(logbyte, index);
    index += 8;
    json.put("start_time", start_time);
    json.put("end_time", end_time);
    int log_count = ByteToInt(logbyte, index);
    index += 4;
    json.put("log_count", log_count);
    JSONArray logs = new JSONArray();
    if (log_format_version == 11) {
      for (int i = 0; i < log_count; i++) {
        JSONObject gps = getGPSLog_v11(logbyte, index);
        index += 41;
        logs.put(gps);
      }
    } else {
      for (int i = 0; i < log_count; i++) {
        JSONObject gps = getGPSLog_v10(logbyte, index);
        index += 20;
        logs.put(gps);
      }
    }
    json.put("logs", logs);
    return json;
  }
  static JSONObject getGPSLog_v11(byte[] log, int start) {
    int index = start;
    JSONObject json = new JSONObject();
    json.put("link_id", ByteToLong(log, index));
    index += 8;
    json.put("x", ByteToInt(log, index));
    index += 4;
    json.put("y", ByteToInt(log, index));
    index += 4;
    json.put("matched_x", ByteToShort(log, index));
    index += 2;
    json.put("matched_y", ByteToShort(log, index));
    index += 2;
    json.put("altitude", ByteToShort(log, index));
    index += 2;
    json.put("speed", ByteToShort(log, index));
    index += 2;
    json.put("angle", ByteToShort(log, index));
    index += 2;
    json.put("pos_error", log[index++]);
    json.put("altitude_error", log[index++]);
    json.put("gps_status", log[index++]);
    json.put("time_interval", ByteToInt(log, index));
    index += 4;
    json.put("gps_time", ByteToLong(log, index));
    return json;
  }
  static JSONObject getGPSLog_v10(byte[] log, int start) {
    int index = start;
    JSONObject json = new JSONObject();
    json.put("x", ByteToInt(log, index));
    index += 4;
    json.put("y", ByteToInt(log, index));
    index += 4;
    json.put("altitude", ByteToShort(log, index));
    index += 2;
    json.put("speed", ByteToShort(log, index));
    index += 2;
    json.put("angle", ByteToShort(log, index));
    index += 2;
    json.put("pos_error", log[index++]);
    json.put("altitude_error", log[index++]);
    json.put("time_interval", ByteToInt(log, index));
    return json;
  }
  static short ByteToShort(byte[] log, int start) {
    short ans = 0;
    for (int i = 0; i < 2; i++) {
      ans = (short)(ans << 8 | (log[start + i] & 0xFF));
    }
    return ans;
  }
  static int ByteToInt(byte[] log, int start) {
    int ans = 0;
    for (int i = 0; i < 4; i++) {
      ans = ans << 8 | (log[start + i] & 0xFF);
    }
    return ans;
  }
  static long ByteToLong(byte[] log, int start) {
    long ans = 0;
    for (int i = 0; i < 8; i++) {
      ans = ans << 8 | (log[start + i] & 0xFF);
    }
    return ans;
  }
}
