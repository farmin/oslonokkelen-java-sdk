package no.kommune.oslo.nokkelen.sdk;

import no.kommune.oslo.nokkelen.sdk.auth.AuthClient;
import no.kommune.oslo.nokkelen.sdk.auth.AuthToken;
import no.kommune.oslo.nokkelen.sdk.auth.ClientCredentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

public class OslonokkelenClient implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(OslonokkelenClient.class);

  private final URI serviceBaseURI;
  private final AdapterController controller;
  private final AuthClient authClient;
  private final ClientCredentials clientCredentials;
  private final OkHttpClient httpClient;

  private AuthToken token;



  public OslonokkelenClient(URI serviceBaseURI,
                            AuthClient authClient,
                            ClientCredentials clientCredentials,
                            AdapterController controller) {

    this.httpClient = new OkHttpClient.Builder()
        .connectTimeout(4, SECONDS)
        .readTimeout(10, MINUTES)
        .writeTimeout(10, SECONDS)
        .pingInterval(10, SECONDS)
        .build();

    this.clientCredentials = clientCredentials;
    this.serviceBaseURI = URI.create(serviceBaseURI.toString() + "/v1/adapter/ws");
    this.authClient = authClient;
    this.controller = controller;
  }

  public void start(LocalState state) {
    MessageRouter router = new MessageRouter(state);


    while (!Thread.currentThread().isInterrupted()) {
      log.info("Connecting to {}", serviceBaseURI);
      var socketListener = new OslonokkelenSocketListener(controller, router);

      try {
        var request = createRequest();
        httpClient.newWebSocket(request, socketListener);

        log.info("Establishing connection...");
        if (!socketListener.awaitConnection(15, SECONDS)) {
          log.warn("Failed to establish connection, will try again in a few seconds");
        }
        else {
          log.info("We are connected, hanging around until the connection goes down..");
          socketListener.awaitClose();
        }
      }
      catch (InterruptedException ex) {
        log.info("Got interrupted");
        Thread.currentThread().interrupt();

        try {
          log.info("Stopping websocket client");
          socketListener.disconnect();
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

  private Request createRequest() {
    AuthToken token = authenticate();

    return new Request.Builder()
        .url(token.accessEndpoint(serviceBaseURI).toString())
        .build();
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

  private AuthToken authenticate() {
    if (token == null) {
      token = authClient.authenticate(clientCredentials);
    }
    if (token.hasExpiredAccessToken()) {
      token = authClient.refresh(clientCredentials, token);
    }

    log.info("Authenticated: {}", token);
    return token;
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


    try {
      log.debug("Stopping http client");
      httpClient.dispatcher().executorService().shutdown();
      httpClient.connectionPool().evictAll();
    }
    catch (Exception ex) {
      log.warn("Failed to stop http client", ex);
    }
  }

}
