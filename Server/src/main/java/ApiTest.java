import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import io.swagger.client.model.SkierVertical;
import java.util.List;

public class ApiTest {

  public static void main(String[] args) {
    Integer resortId = 56;

    try {
      SkiersApi api = new SkiersApi();
      api.getApiClient().setBasePath("http://localhost:8080/Server_war");
      SkierVertical vertical =
          api.getSkierResortTotals(
              555, List.of("res1", "res2", "res3"), List.of("2016", "2017", "2020"));
      LiftRide ride = new LiftRide().liftID(44).time(300).waitTime(8);

      ApiResponse<Void> response =
          api.writeNewLiftRideWithHttpInfo(ride, resortId, "2022", "365", 40982);
      System.out.println(response.getStatusCode());
    } catch (ApiException e) {
      System.err.println("Exception occurred!");
      e.printStackTrace();
    }
  }
}
