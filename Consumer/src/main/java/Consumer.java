import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class Consumer {

  protected static final String QUEUE_NAME = "liftride";
  protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  protected static final Map<Integer, AtomicInteger> liftRideMap = new ConcurrentHashMap<>();
  private static final Integer NUM_THREADS = 20;

  public static void main(String[] args) throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    Connection connection = factory.newConnection();

    ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

    for (int i = 0; i < NUM_THREADS; i++) {
      pool.execute(new ConsumerRunnable(QUEUE_NAME, connection));
    }
  }
}
