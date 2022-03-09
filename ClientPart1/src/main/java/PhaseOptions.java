public class PhaseOptions {

  private final String name;
  private final Integer numThreads;
  private final Double threshold;
  private final Integer startTime;
  private final Integer endTime;
  private final Integer numReqs;

  public PhaseOptions(
      String name,
      Integer numThreads,
      Double threshold,
      Integer startTime,
      Integer endTime,
      Integer numReqs) {
    this.name = name;
    this.numThreads = numThreads;
    this.threshold = threshold;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numReqs = numReqs;
  }

  public String getName() {
    return name;
  }

  public Integer getNumThreads() {
    return numThreads;
  }

  public Double getThreshold() {
    return threshold;
  }

  public Integer getStartTime() {
    return startTime;
  }

  public Integer getEndTime() {
    return endTime;
  }

  public Integer getNumReqs() {
    return numReqs;
  }
}
