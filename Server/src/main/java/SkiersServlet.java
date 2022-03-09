import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.SkierVertical;
import io.swagger.client.model.SkierVerticalResorts;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkiersServlet extends HttpServlet {

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private final String QUEUE_NAME = "liftride";
  private Connection conn;

  @Override
  public void init() throws ServletException {
    super.init();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");

    try {
      conn = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      System.err.println("Unable to create RabbitMQ connection");
      e.printStackTrace();
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path");
      return;
    }

    String[] urlParts = urlPath.split("/");

    if (!isUrlValid(urlPath)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path or parameters supplied");
    } else if (Endpoint.GET_LIFT_RIDES.pattern.matcher(urlPath).matches()) {
      if (Integer.parseInt(urlParts[5]) < 0 || Integer.parseInt(urlParts[5]) > 365) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write("Invalid day value");
        return;
      }

      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write(34507);
    } else if (req.getParameter("resort") == null) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Missing parameter: 'resort'");
    } else {
      res.setStatus(HttpServletResponse.SC_OK);

      String json =
          gson.toJson(
              new SkierVertical()
                  .addResortsItem(new SkierVerticalResorts().seasonID("2016").totalVert(855))
                  .addResortsItem(new SkierVerticalResorts().seasonID("2017").totalVert(777))
                  .addResortsItem(new SkierVerticalResorts().seasonID("2020").totalVert(900)));
      res.getWriter().write(json);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path");
      return;
    }

    JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

    String[] urlParts = urlPath.split("/");

    if (!Endpoint.POST_LIFT_RIDES.pattern.matcher(urlPath).matches()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path or parameters supplied");
    } else {
      // Check if POST body parameters are valid
      if (!validateParameterValues(body, res)) return;

      Integer skierID = Integer.parseInt(urlParts[7]);

      Channel channel = conn.createChannel();
      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      JsonObject message = createMessage(body, skierID);

      channel.basicPublish("", QUEUE_NAME, null, gson.toJson(message).getBytes());
      System.out.println(" [x] Sent '" + message + "'");

      res.setStatus(HttpServletResponse.SC_CREATED);
      res.getWriter().write("Lift ride created!");
    }
  }

  private boolean isUrlValid(String url) {
    for (Endpoint endpoint : Endpoint.values()) {
      Pattern pattern = endpoint.pattern;

      if (pattern.matcher(url).matches()) {
        return true;
      }
    }

    return false;
  }

  private boolean validateParameterValues(JsonObject body, HttpServletResponse res)
      throws IOException {
    PrintWriter writer = res.getWriter();

    Map<String, String> params = new HashMap<>();
    params.put("time", null);
    params.put("liftID", null);
    params.put("waitTime", null);

    for (String param : params.keySet()) {
      JsonElement value = body.get(param);

      if (value == null) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.write("Missing parameter: '" + param + "'");
        return false;
      }

      params.replace(param, value.getAsString());
    }

    // TODO: Simplify this using a Parameter class with an 'isValid' method
    return isValidValue("time", params.get("time"), 1, 420, res)
        && isValidValue("liftID", params.get("liftID"), 1, Integer.MAX_VALUE, res)
        && isValidValue("waitTime", params.get("waitTime"), 0, Integer.MAX_VALUE, res);
  }

  private boolean isValidValue(
      String name, String value, Integer lowerBound, Integer upperBound, HttpServletResponse res)
      throws IOException {
    try {
      Integer parsedVal = Integer.parseInt(value);

      if (!isWithinRange(parsedVal, lowerBound, upperBound)) throw new NumberFormatException();
    } catch (NumberFormatException nfe) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Invalid value for parameter '" + name + "': " + value);
      return false;
    }

    return true;
  }

  private boolean isWithinRange(Integer value, Integer lowerBound, Integer upperBound) {
    return value >= lowerBound && value < upperBound;
  }

  private JsonObject createMessage(JsonObject body, Integer skierID) {
    JsonObject message = new JsonObject();
    message.add("liftID", body.get("liftID"));
    message.add("skierID", new JsonPrimitive(skierID));

    return message;
  }

  private enum Endpoint {
    GET_LIFT_RIDES(Pattern.compile("/\\d+/seasons/\\d+/days/\\d+/skiers/\\d+")),
    POST_LIFT_RIDES(Pattern.compile("/\\d+/seasons/\\d+/days/\\d+/skiers/\\d+")),
    GET_VERTICAL(Pattern.compile("/\\d+/vertical"));

    public final Pattern pattern;

    Endpoint(Pattern pattern) {
      this.pattern = pattern;
    }
  }
}
