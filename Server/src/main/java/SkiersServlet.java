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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
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
  private final Integer NUM_CHANNELS = 20;
  private Connection conn;
  private BlockingQueue<Channel> pool;

  /**
   * Overridden init method that creates a connection to the RabbitMQ node and creates a shared pool
   * of publishing channels for the Tomcat threads to use.
   *
   * @throws ServletException When an exception occurs that interrupts the servlet's normal
   *     operation.
   */
  @Override
  public void init() throws ServletException {
    super.init();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.209.65.46");
    factory.setUsername("server");
    factory.setPassword("admin");

    try {
      conn = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      System.err.println("Unable to create RabbitMQ connection");
      e.printStackTrace();
    }

    // The shared pool of channels is implemented using a BlockingQueue. The threads will take and
    // replace the channels to publish messages
    pool = new LinkedBlockingDeque<>();
    for (int i = 0; i < NUM_CHANNELS; i++) {
      try {
        Channel channel = conn.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        pool.add(channel);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    // Check if URL path is able to be validated
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path");
      return;
    }

    String[] urlParts = urlPath.split("/");

    // First confirm that passed in URL is one of the recognized endpoints
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

      // Send back dummy data as a response
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

    // Check if URL path can be validated
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

      JsonObject message = createMessage(body, skierID);

      try {
        Channel channel = pool.take();

        channel.basicPublish("", QUEUE_NAME, null, gson.toJson(message).getBytes());
        System.out.println(" [x] Sent '" + message + "'");

        res.setStatus(HttpServletResponse.SC_CREATED);
        res.getWriter().write("Lift ride created!");

        pool.add(channel);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Helper function to determine if the passed in URL matches a recognized endpoint.
   *
   * @param url The URL to be validated.
   * @return A boolean indicating if the URL is among the recognized endpoints.
   */
  private boolean isUrlValid(String url) {
    for (Endpoint endpoint : Endpoint.values()) {
      Pattern pattern = endpoint.pattern;

      if (pattern.matcher(url).matches()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Helper function to determine if a POST request body is valid.
   *
   * @param body The JSON body to be validated.
   * @param res The response object.
   * @return A boolean indicating if the request body is valid.
   * @throws IOException When an IO error occurs when obtaining the writer for the response.
   */
  private boolean validateParameterValues(JsonObject body, HttpServletResponse res)
      throws IOException {
    PrintWriter writer = res.getWriter();

    Map<String, Parameter> params = new HashMap<>();
    params.put("time", new Parameter("time", null, 1, 420));
    params.put("liftID", new Parameter("liftID", null, 1, Integer.MAX_VALUE));
    params.put("waitTime", new Parameter("waitTime", null, 0, Integer.MAX_VALUE));

    for (String param : params.keySet()) {
      JsonElement value = body.get(param);
      Parameter p = params.get(param);

      if (value == null) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        writer.write("Missing parameter: '" + param + "'");
        return false;
      }

      p.setValue(value.getAsString());
      if (!p.isValid(res)) return false;
    }

    return true;
  }

  /**
   * Helper function to create the message to be published to the messaging queue.
   *
   * @param body The request body from which some parameters will be used for the new message.
   * @param skierID The skier ID value, obtained from the URL of the request.
   * @return A new JsonObject containing the information that will be sent to the queue.
   */
  private JsonObject createMessage(JsonObject body, Integer skierID) {
    JsonObject message = new JsonObject();
    message.add("time", body.get("time"));
    message.add("liftID", body.get("liftID"));
    message.add("waitTime", body.get("waitTime"));
    message.add("skierID", new JsonPrimitive(skierID));

    return message;
  }

  /** Enum containing the patterns for each valid URL in the servlet. */
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
