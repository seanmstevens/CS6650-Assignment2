import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Consumer {

  protected static final String QUEUE_NAME = "liftride";
  protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  protected static final Map<Integer, ConcurrentLinkedQueue<Integer>> liftRideMap =
      new ConcurrentHashMap<>();
  private static final Integer NUM_THREADS = 20;

  public static void main(String[] args) throws IOException, TimeoutException {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.209.65.46");
    factory.setUsername("consumer");
    factory.setPassword("admin");

    Connection connection = factory.newConnection();

    // Create a fixed size thread pool. This allows the JVM to schedule threads in the most
    // efficient way possible
    ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);

    for (int i = 0; i < NUM_THREADS; i++) {
      pool.execute(new ConsumerRunnable(QUEUE_NAME, connection));
    }
  }
}
