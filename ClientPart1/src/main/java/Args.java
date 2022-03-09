import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import java.net.InetSocketAddress;

public class Args {

  private static final Integer MAX_THREADS = 1024;
  private static final Integer MAX_SKIERS = 100000;
  private static final Integer MIN_LIFTS = 5;
  private static final Integer MAX_LIFTS = 60;
  private static final Integer MAX_RUNS = 20;

  @Parameter(
      names = {"-l", "--numLifts"},
      description = "Number of ski lifts",
      validateWith = NumLifts.class)
  private Integer numLifts = 40;

  @Parameter(
      names = {"-r", "--numRuns"},
      description = "Mean number of ski lifts each skier rides each day",
      validateWith = NumRuns.class)
  private Integer numRuns = 10;

  @Parameter(
      names = {"-t", "--numThreads"},
      description = "Number of threads to execute",
      validateWith = NumThreads.class,
      required = true)
  private Integer numThreads;

  @Parameter(
      names = {"-s", "--numSkiers"},
      description = "Number of skiers to generate lift rides for",
      validateWith = NumSkiers.class,
      required = true)
  private Integer numSkiers;

  @Parameter(
      description = "The IP address and port of the server",
      converter = AddressConverter.class,
      required = true)
  private InetSocketAddress address;

  public Integer getNumThreads() {
    return numThreads;
  }

  public Integer getNumSkiers() {
    return numSkiers;
  }

  public Integer getNumLifts() {
    return numLifts;
  }

  public Integer getNumRuns() {
    return numRuns;
  }

  public InetSocketAddress getAddress() {
    return address;
  }

  public static class NumThreads implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      int n = Integer.parseInt(value);
      if (n < 0 || n > MAX_THREADS) {
        throw new ParameterException(
            "Parameter "
                + name
                + " should be within range 0 - "
                + MAX_THREADS
                + " (found "
                + value
                + ")");
      }
    }
  }

  public static class NumSkiers implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      int n = Integer.parseInt(value);
      if (n < 1 || n > MAX_SKIERS) {
        throw new ParameterException(
            "Parameter "
                + name
                + " should be within range 1 - "
                + MAX_SKIERS
                + " (found "
                + value
                + ")");
      }
    }
  }

  public static class NumLifts implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      int n = Integer.parseInt(value);
      if (n < MIN_LIFTS || n > MAX_LIFTS) {
        throw new ParameterException(
            "Parameter "
                + name
                + " should be within range "
                + MIN_LIFTS
                + " - "
                + MAX_LIFTS
                + " (found "
                + value
                + ")");
      }
    }
  }

  public static class NumRuns implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
      int n = Integer.parseInt(value);
      if (n < 1 || n > MAX_RUNS) {
        throw new ParameterException(
            "Parameter "
                + name
                + " should be within range 1 - "
                + MAX_RUNS
                + " (found "
                + value
                + ")");
      }
    }
  }

  public static class AddressConverter implements IStringConverter<InetSocketAddress> {

    @Override
    public InetSocketAddress convert(String s) {
      String[] addressParts = s.split(":");

      String host = addressParts[0];
      int port = Integer.parseInt(addressParts[1]);

      return new InetSocketAddress(host, port);
    }
  }
}
