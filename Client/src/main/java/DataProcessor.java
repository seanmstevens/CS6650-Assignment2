import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class DataProcessor {

  private static final String[] HEADERS = {
    "start_time", "latency", "request_type", "response_code"
  };

  private final List<LatencyRecord> recordList;
  private final FileWriter out;

  public DataProcessor(String filename) throws IOException {
    this.recordList = new ArrayList<>();
    this.out = new FileWriter(filename);
  }

  public synchronized void addRecords(List<LatencyRecord> items) {
    recordList.addAll(items);
  }

  public double getMeanResponseTime() {
    return recordList.stream().mapToLong(LatencyRecord::getLatency).average().orElse(0.0);
  }

  public long getMedianResponseTime() {
    Collections.sort(recordList);
    int length = recordList.size();

    if (length % 2 == 0) {
      return (recordList.get(length / 2).getLatency() + recordList.get(length / 2 - 1).getLatency())
          / 2;
    }

    return recordList.get(length / 2).getLatency();
  }

  public long getP99() {
    Collections.sort(recordList);
    int length = recordList.size();
    int p99Index = (int) Math.ceil(length * 0.99);

    return recordList.get(p99Index).getLatency();
  }

  public long getMaxResponseTime() {
    return recordList.stream().mapToLong(LatencyRecord::getLatency).max().orElse(0);
  }

  public long getMinResponseTime() {
    return recordList.stream().mapToLong(LatencyRecord::getLatency).min().orElse(0);
  }

  public void writeCSV() throws IOException {
    try (final CSVPrinter printer =
        new CSVPrinter(out, CSVFormat.Builder.create().setHeader(HEADERS).build())) {
      for (LatencyRecord record : recordList) {
        printer.printRecord(
            record.getStartTime(),
            record.getLatency(),
            record.getRequestType(),
            record.getResponseCode());
      }
    }
  }
}
