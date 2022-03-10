import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class ConsumerRunnable implements Runnable {

  private final String queueName;
  private final Connection conn;

  public ConsumerRunnable(String queueName, Connection conn) {
    this.queueName = queueName;
    this.conn = conn;
  }

  @Override
  public void run() {
    try {
      final Channel channel = conn.createChannel();
      channel.queueDeclare(queueName, false, false, false, null);
      System.out.println(
          " [*] Thread " + Thread.currentThread().getId() + " waiting for messages.");

      final DeliverCallback callback =
          (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            JsonObject body = Consumer.gson.fromJson(message, JsonObject.class);

            Integer skierID = Integer.parseInt(body.get("skierID").getAsString());

            AtomicInteger value = Consumer.liftRideMap.get(skierID);
            if (value == null) {
              Consumer.liftRideMap.put(skierID, new AtomicInteger(0));
            } else {
              Consumer.liftRideMap.get(skierID).incrementAndGet();
            }

            System.out.println(
                " [x] Thread " + Thread.currentThread().getId() + " received '" + body + "'");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          };

      channel.basicConsume(queueName, false, callback, consumerTag -> {});
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
