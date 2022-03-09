import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.client.model.APIStats;
import io.swagger.client.model.APIStatsEndpointStats;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "StatisticsServlet", value = "/StatisticsServlet")
public class StatisticsServlet extends HttpServlet {

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    if (urlPath != null) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path");
      return;
    }

    String json =
        gson.toJson(
            new APIStats()
                .addEndpointStatsItem(
                    new APIStatsEndpointStats()
                        .max(198)
                        .mean(11)
                        .URL("/resorts")
                        .operation("GET")));

    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write(json);
  }
}
