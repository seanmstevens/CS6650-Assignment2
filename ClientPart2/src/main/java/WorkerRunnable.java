import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerRunnable implements Runnable {

  private static final Integer MAX_RETRIES = 5;

  private final Integer startId;
  private final Integer endId;
  private final Integer startTime;
  private final Integer endTime;
  private final String serverUrl;
  private final Integer numReqs;
  private final Integer numLifts;
  private final CountDownLatch latch;
  private final CountDownLatch totalLatch;
  private final AtomicInteger numSuccessful;
  private final AtomicInteger numFailed;
  private final DataProcessor processor;

  public WorkerRunnable(
      Integer startId,
      Integer endId,
      Integer startTime,
      Integer endTime,
      String serverUrl,
      Integer numReqs,
      Integer numLifts,
      CountDownLatch latch,
      CountDownLatch totalLatch,
      AtomicInteger numSuccessful,
      AtomicInteger numFailed,
      DataProcessor processor) {
    this.startId = startId;
    this.endId = endId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.serverUrl = serverUrl;
    this.numReqs = numReqs;
    this.numLifts = numLifts;
    this.latch = latch;
    this.totalLatch = totalLatch;
    this.numSuccessful = numSuccessful;
    this.numFailed = numFailed;
    this.processor = processor;
  }

  @Override
  public void run() {
    SkiersApi apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath(serverUrl);

    List<LatencyRecord> latencyList = new ArrayList<>();
    int numSuccessful = 0;
    int numFailed = 0;

    for (int i = 0; i < numReqs; i++) {
      int id = ThreadLocalRandom.current().nextInt(startId, endId + 1);
      int time = ThreadLocalRandom.current().nextInt(startTime, endTime + 1);
      int liftId = ThreadLocalRandom.current().nextInt(1, numLifts + 1);
      int waitTime = ThreadLocalRandom.current().nextInt(1, 11);

      LiftRide ride = new LiftRide().time(time).liftID(liftId).waitTime(waitTime);

      boolean success = false;
      int numTries = 0;

      while (!success && numTries < MAX_RETRIES) {
        long start = System.currentTimeMillis();

        try {
          ApiResponse<Void> response =
              apiInstance.writeNewLiftRideWithHttpInfo(ride, 56, "2022", "200", id);
          long end = System.currentTimeMillis();

          if (response.getStatusCode() >= 400) {
            latencyList.add(
                new LatencyRecord(
                    start, end - start, "POST", String.valueOf(response.getStatusCode())));
            sleepThread(numTries++);
            continue;
          }

          latencyList.add(
              new LatencyRecord(
                  start, end - start, "POST", String.valueOf(response.getStatusCode())));
          numSuccessful++;
          success = true;
        } catch (ApiException e) {
          // System.err.println("POST request failure: " + e.getMessage() + ", " + e.getCode());
          long end = System.currentTimeMillis();

          latencyList.add(
              new LatencyRecord(start, end - start, "POST", String.valueOf(e.getCode())));

          sleepThread(numTries++);
        }
      }

      if (!success) {
        numFailed++;
      }
    }

    this.numSuccessful.addAndGet(numSuccessful);
    this.numFailed.addAndGet(numFailed);
    this.processor.addRecords(latencyList);

    this.latch.countDown();
    this.totalLatch.countDown();
  }

  private void sleepThread(Integer numTries) {
    try {
      Thread.sleep(getWaitTime(numTries));
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }
  }

  private Integer getWaitTime(Integer n) {
    return 2 ^ n;
  }
}
