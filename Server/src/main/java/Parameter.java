import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/** A class representing an HTTP JSON body integer parameter. */
public class Parameter {

  private final String name;
  private final Integer lowerBound;
  private final Integer upperBound;
  private String value;

  /**
   * Constructor for Parameter class.
   *
   * @param name The name of the parameter, typically the key in the JSON body.
   * @param value The unparsed value for the parameter.
   * @param lowerBound The lower integer bound that the parameter value will be checked against.
   * @param upperBound The upper integer bound that the parameter value will be checked against.
   */
  public Parameter(String name, String value, Integer lowerBound, Integer upperBound) {
    this.name = name;
    this.value = value;
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * Getter method for the parameter name.
   *
   * @return The parameter name as a String.
   */
  public String getName() {
    return name;
  }

  /**
   * Getter method for the unparsed parameter value.
   *
   * @return The parameter value as a String.
   */
  public String getValue() {
    return value;
  }

  /**
   * Setter method for the unparsed parameter value.
   *
   * @param value The unparsed parameter value as a String.
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Validation function used to verify that the parameter is not null, is parsable as an integer,
   * and is within the expected range.
   *
   * @param res The HttpServletResponse object that the error message will be written to if
   *     validation fails.
   * @return A boolean indicating if the parameter value is valid.
   * @throws IOException When an input or output exception occurs when getting the response writer.
   */
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

  /**
   * Helper function to determine if integer is within bounds.
   *
   * @param parsedVal The integer value to be validated.
   * @return A boolean indicating if the value is within range.
   */
  private boolean isWithinRange(Integer parsedVal) {
    return parsedVal >= lowerBound && parsedVal < upperBound;
  }
}
