public class LatencyRecord implements Comparable<LatencyRecord> {

  private final long startTime;
  private final long latency;
  private final String requestType;
  private final String responseCode;

  public LatencyRecord(long startTime, long latency, String requestType, String responseCode) {
    this.startTime = startTime;
    this.latency = latency;
    this.requestType = requestType;
    this.responseCode = responseCode;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getLatency() {
    return latency;
  }

  public String getRequestType() {
    return requestType;
  }

  public String getResponseCode() {
    return responseCode;
  }

  @Override
  public int compareTo(LatencyRecord o) {
    return (int) (this.getLatency() - o.getLatency());
  }
}
