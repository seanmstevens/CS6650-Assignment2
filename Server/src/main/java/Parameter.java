import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
 * A class representing an HTTP JSON body integer parameter.
 */
public class Parameter {

  private final String name;
  private final Integer lowerBound;
  private final Integer upperBound;
  private String value;

  public Parameter(String name, String value, Integer lowerBound, Integer upperBound)
      throws NumberFormatException {
    this.name = name;
    this.value = value;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Integer getLowerBound() {
    return lowerBound;
  }

  public Integer getUpperBound() {
    return upperBound;
  }

  public boolean isValid(HttpServletResponse res) throws IOException {
    try {
      Integer parsedVal = Integer.parseInt(value);

      if (!isWithinRange(parsedVal)) throw new NumberFormatException();
    } catch (NumberFormatException nfe) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write("Invalid value for parameter '" + name + "': " + value);
      return false;
    }

    return true;
  }

  private boolean isWithinRange(Integer parsedVal) {
    return parsedVal >= lowerBound && parsedVal < upperBound;
  }
}
