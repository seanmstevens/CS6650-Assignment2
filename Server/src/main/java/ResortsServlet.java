import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.swagger.client.model.ResortsList;
import io.swagger.client.model.ResortsListResorts;
import io.swagger.client.model.SeasonsList;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ResortServlet", value = "/ResortServlet")
public class ResortsServlet extends HttpServlet {

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    if (urlPath == null || urlPath.isEmpty() || Endpoint.BASE.pattern.matcher(urlPath).matches()) {
      res.setStatus(HttpServletResponse.SC_OK);
      String json =
          gson.toJson(
              new ResortsList()
                  .addResortsItem(new ResortsListResorts().resortID(56).resortName("Test Resort")));

      res.getWriter().write(json);
      return;
    }

    if (!isUrlValid(urlPath)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Invalid path or parameters supplied");
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      String json =
          gson.toJson(
              new SeasonsList()
                  .addSeasonsItem("2019")
                  .addSeasonsItem("2020")
                  .addSeasonsItem("2021"));

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

    if (body.get("year") == null) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Missing parameter: 'year'");
      return;
    }

    if (!Endpoint.POST_SEASONS.pattern.matcher(urlPath).matches()) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Invalid path or parameters supplied");
    } else {
      res.setStatus(HttpServletResponse.SC_CREATED);
      res.getWriter().write("Season created!");
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

  private enum Endpoint {
    BASE(Pattern.compile("/?")),
    GET_SEASONS(Pattern.compile("/\\d+/seasons")),
    POST_SEASONS(Pattern.compile("/\\d+/seasons"));

    public final Pattern pattern;

    Endpoint(Pattern pattern) {
      this.pattern = pattern;
    }
  }
}
