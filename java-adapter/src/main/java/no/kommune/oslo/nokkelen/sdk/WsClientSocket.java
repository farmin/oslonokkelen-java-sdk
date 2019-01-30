package no.kommune.oslo.nokkelen.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.kommune.oslo.nokkelen.api.messages.AckMessage;
import no.kommune.oslo.nokkelen.api.messages.AdapterMessage;
import no.kommune.oslo.nokkelen.api.messages.ExecuteActionMessage;
import no.kommune.oslo.nokkelen.api.messages.PingMessage;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class WsClientSocket implements MessageSink {

  private static final Logger log = LoggerFactory.getLogger(WsClientSocket.class);

  private final CountDownLatch closeLatch = new CountDownLatch(1);

  private final AdapterController controller;
  private final MessageRouter messageRouter;
  private final ObjectMapper jackson;

  private Session session;

  WsClientSocket(AdapterController controller, MessageRouter messageRouter, ObjectMapper jackson) {
    this.controller = controller;
    this.messageRouter = messageRouter;
    this.jackson = jackson;
  }

  @OnWebSocketConnect
  @SuppressWarnings("unused")
  public void onConnect(Session session) {
    log.info("Connected to {}", session.getRemoteAddress());

    this.session = session;
    this.messageRouter.connected(this);
    this.controller.onConnection(this);
  }

  @Override
  public void sendMessage(AdapterMessage message) {
    sendMessage(session, message);
  }

  private void sendMessage(Session session, AdapterMessage message) {
    try {
      String payload = toJson(message);
      RemoteEndpoint remote = session.getRemote();
      remote.sendString(payload);
    }
    catch (WebSocketException ex) {
      try {
        log.warn("Got websocket exception, will try to re-connect", ex);
        session.disconnect();
      }
      catch (IOException exx) {
        log.error("Failed to disconnect", exx);
      }
    }
    catch (Exception ex) {
      throw new IllegalStateException("Failed to send " + message, ex);
    }
  }


  @OnWebSocketMessage
  @SuppressWarnings("unused")
  public void onMessage(String msg) {
    try {
      JsonNode message = jackson.readTree(msg);
      String messageType = message.get("type").textValue();

      switch (messageType) {
        case "execute-action":
          ExecuteActionMessage executeCommand = fromJson(message, ExecuteActionMessage.class);
          try (MDC.MDCCloseable ignored = MDC.putCloseable("correlation-id", executeCommand.getRequestId())) {
            log.info("Executing {}", executeCommand.getRequestId());
            messageRouter.execute(this, executeCommand);
          }
          break;

        case "ping":
          PingMessage pingMessage = fromJson(message, PingMessage.class);
          messageRouter.ping(this, pingMessage);
          break;

        case "ack":
          AckMessage ackMessage = fromJson(message, AckMessage.class);
          controller.ack(ackMessage);
          break;

        default:
          log.warn("Unknown message type: {}", messageType);
      }
    }
    catch (Exception ex) {
      throw new IllegalStateException("Error handling message: " + msg, ex);
    }
  }


  private String toJson(AdapterMessage obj) {
    try {
      return jackson.writeValueAsString(obj);
    }
    catch (JsonProcessingException ex) {
      throw new IllegalStateException("Failed to turn " + obj + " into json", ex);
    }
  }

  private <T extends AdapterMessage> T fromJson(JsonNode json, Class<T> target) {
    try {
      return jackson.treeToValue(json, target);
    }
    catch (Exception ex) {
      throw new IllegalStateException("Failed to parse " + json + " into " + target, ex);
    }
  }


  @OnWebSocketClose
  @SuppressWarnings("unused")
  public void onClose(int statusCode, String reason) {
    log.info("Connection closed with status {} (reason: {})", statusCode, reason);
    this.controller.onDisconnected();
    this.closeLatch.countDown(); // trigger latch
  }

  void awaitClose() throws InterruptedException {
    log.debug("Waiting for socket to close...");
    this.closeLatch.await();
  }


}
