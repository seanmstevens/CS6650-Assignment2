import io.swagger.client.ApiException;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.ArrayList;
import java.util.List;

public class LatencyTest {

  public static void main(String[] args) throws ApiException {
    SkiersApi api = new SkiersApi();
    api.getApiClient().setBasePath("http://34.221.5.102:8080/Server_war");
    List<Long> responseTimes = new ArrayList<>();

    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000; i++) {
      long reqStart = System.currentTimeMillis();

      LiftRide liftRide = new LiftRide().liftID(2).time(4);
      api.writeNewLiftRide(liftRide, 56, "2022", "203", 4444);

      responseTimes.add(System.currentTimeMillis() - reqStart);
    }
    long end = System.currentTimeMillis();

    System.out.println(
        "Mean response time: "
            + responseTimes.stream().mapToDouble(a -> a).average().getAsDouble());
    System.out.println("Total execution time (sec): " + ((float) (end - start)) / 1000);

    System.out.println("Total actual throughput: " + (1000 / ((float) (end - start) / 1000)));
  }
}
