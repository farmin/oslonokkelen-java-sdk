package no.oslo.kommune.oslonokkelen.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.oslo.kommune.oslonokkelen.sdk.auth.AuthClient;
import no.oslo.kommune.oslonokkelen.sdk.auth.AuthToken;
import no.oslo.kommune.oslonokkelen.sdk.auth.ClientCredentials;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

public class OslonokkelenClient implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(OslonokkelenClient.class);

  private final URI serviceBaseURI;
  private final ObjectMapper jackson;
  private final AdapterController controller;
  private final AuthClient authClient;
  private final ClientCredentials clientCredentials;

  private AuthToken token;



  public OslonokkelenClient(URI serviceBaseURI,
                            ObjectMapper jackson,
                            AuthClient authClient,
                            ClientCredentials clientCredentials,
                            AdapterController controller) {

    this.clientCredentials = clientCredentials;
    this.serviceBaseURI = serviceBaseURI;
    this.authClient = authClient;
    this.controller = controller;
    this.jackson = jackson;
  }

  public void start(LocalState state) {
    HttpClient httpClient = new HttpClient();
    httpClient.setConnectTimeout(Duration.ofSeconds(10).toMillis());
    httpClient.setAddressResolutionTimeout(Duration.ofSeconds(2).toMillis());
    httpClient.setIdleTimeout(Duration.ofMinutes(60).toMillis());

    WebSocketClient websocketClient = new WebSocketClient(httpClient);
    websocketClient.setMaxIdleTimeout(Duration.ofMinutes(60).toMillis());

    MessageRouter router = new MessageRouter(state);


    while (!Thread.currentThread().isInterrupted()) {
      log.info("Connecting to {}...", serviceBaseURI);

      try {
        authenticate();
        websocketClient.start();

        WsClientSocket socket = new WsClientSocket(controller, router, jackson);
        connect(websocketClient, socket);

        log.info("Waiting for socket to close..");
        socket.awaitClose();
      }
      catch (InterruptedException ex) {
        log.info("Got interrupted");
        Thread.currentThread().interrupt();

        try {
          log.info("Stopping websocket client");
          websocketClient.stop();
        }
        catch (Exception ex2) {
          log.warn("Got exception trying to stop client", ex2);
        }
      }
      catch (Exception ex) {
        log.error("Something didn't go as planned..", ex);
      }

      sleep();
    }
  }

  private void sleep() {
    if (!Thread.currentThread().isInterrupted()) {
      log.info("Sleeping a bit before re-connecting");

      try {
        Thread.sleep(15_000);
      }
      catch (InterruptedException ignored) {
        log.info("Got interrupted before trying again");
        Thread.currentThread().interrupt();
      }
    }
  }

  private void authenticate() {
    if (token == null) {
      token = authClient.authenticate(clientCredentials);
    }
    if (token.hasExpiredAccessToken()) {
      token = authClient.refresh(clientCredentials, token);
    }

    log.info("Authenticated");
  }

  private void connect(WebSocketClient client, WsClientSocket socket) throws Exception {
    URI uri = URI.create(serviceBaseURI + "/v1/adapter/ws");
    ClientUpgradeRequest request = new ClientUpgradeRequest();
    Future<Session> connect = client.connect(socket, token.accessEndpoint(uri), request);
    connect.get(5, SECONDS);
  }

  @Override
  public void close() {
    log.debug("Stopping websocket client");
    stop();
  }

  public void stop() {
    try {
      if (token != null) {
        if (token.hasExpiredRefreshToken()) {
          log.debug("Token has expired, will skip logout");
        }
        else {
          log.debug("Token is still valid, issuing logout request");
          authClient.logout(clientCredentials, token);
        }
      }
    }
    catch (Exception ex) {
      log.warn("Something went wrong during stop", ex);
    }
  }

}
