package no.kommune.oslo.nokkelen.sdk;

import com.fasterxml.jackson.databind.JsonNode;
import no.kommune.oslo.nokkelen.api.messages.AckMessage;
import no.kommune.oslo.nokkelen.api.messages.AdapterMessage;
import no.kommune.oslo.nokkelen.api.messages.ExecuteActionMessage;
import no.kommune.oslo.nokkelen.api.messages.PingMessage;
import no.kommune.oslo.nokkelen.sdk.serialization.JsonSerializer;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OslonokkelenSocketListener extends WebSocketListener implements MessageSink {

  private static final Logger log = LoggerFactory.getLogger(OslonokkelenSocketListener.class);

  private final CountDownLatch closeLatch = new CountDownLatch(1);
  private final CountDownLatch connectLatch = new CountDownLatch(1);

  private final AdapterController controller;
  private final MessageRouter messageRouter;

  private WebSocket socket;

  OslonokkelenSocketListener(AdapterController controller, MessageRouter messageRouter) {
    this.controller = controller;
    this.messageRouter = messageRouter;
  }

  @Override
  public void onOpen(WebSocket webSocket, Response response) {
    log.info("Connected to {}", webSocket);

    this.socket = webSocket;
    this.messageRouter.connected(this);
    this.controller.onConnection(this);
    this.connectLatch.countDown();
  }

  @Override
  public void onMessage(WebSocket webSocket, String text) {
    try {
      JsonNode message = JsonSerializer.readTree(text);
      String messageType = message.get("type").textValue();

      switch (messageType) {
        case "execute-action":
          ExecuteActionMessage executeCommand = JsonSerializer.fromJson(text, ExecuteActionMessage.class);
          try (MDC.MDCCloseable ignored = MDC.putCloseable("correlation-id", executeCommand.getRequestId())) {
            log.info("Executing {}", executeCommand.getRequestId());
            messageRouter.execute(this, executeCommand);
          }
          break;

        case "ping":
          PingMessage pingMessage = JsonSerializer.fromJson(text, PingMessage.class);
          messageRouter.ping(this, pingMessage);
          break;

        case "ack":
          AckMessage ackMessage = JsonSerializer.fromJson(text, AckMessage.class);
          controller.ack(ackMessage);
          break;

        default:
          log.warn("Unknown message type: {}", messageType);
      }
    }
    catch (Exception ex) {
      throw new IllegalStateException("Error handling message: " + text, ex);
    }
  }

  @Override
  public void onMessage(WebSocket webSocket, ByteString bytes) {
    log.warn("Got unsupported byte message");
  }

  @Override
  public void onClosing(WebSocket webSocket, int code, String reason) {
  }

  @Override
  public void onClosed(WebSocket webSocket, int code, String reason) {
    log.info("Connection closed with status {} (reason: {})", code, reason);
    this.controller.onDisconnected();
    this.closeLatch.countDown(); // trigger latch
  }

  @Override
  public void onFailure(WebSocket webSocket, Throwable trouble, Response response) {
    log.error("Something failed, we will terminate the connection: ", trouble);
    disconnect();
    this.closeLatch.countDown();
    this.connectLatch.countDown();
  }

  @Override
  public void sendMessage(AdapterMessage message) {
    log.trace("Sending: {}", message);
    String payload = JsonSerializer.toJson(message);
    socket.send(payload);
  }

  void awaitClose() throws InterruptedException {
    log.debug("Waiting for socket to close...");
    this.closeLatch.await();
  }

  boolean awaitConnection(int time, TimeUnit unit) throws InterruptedException {
    try {
      return connectLatch.await(time, unit);
    }
    catch (InterruptedException ex) {
      log.warn("Got interrupted while waiting for connection...");
      Thread.currentThread().interrupt();
      throw ex;
    }
  }

  void disconnect() {
    if (socket != null) {
      log.info("Closing websocket");
      socket.close(1000, "Bye!");
    }
    else {
      log.info("Not connected");
    }
  }

}
