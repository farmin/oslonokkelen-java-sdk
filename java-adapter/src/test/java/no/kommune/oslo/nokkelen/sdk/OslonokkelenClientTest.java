package no.kommune.oslo.nokkelen.sdk;

import no.kommune.oslo.nokkelen.api.messages.AckMessage;
import no.kommune.oslo.nokkelen.sdk.auth.AuthClient;
import no.kommune.oslo.nokkelen.sdk.auth.ClientCredentials;
import no.kommune.oslo.nokkelen.sdk.auth.KeycloakAuthClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class OslonokkelenClientTest {


  @Test
  @DisplayName("x")
  void x() {
    AuthClient authClient = new KeycloakAuthClient(URI.create("http://keycloak:8080"), "oslonokkelen");
    ClientCredentials credentials = new ClientCredentials("adapter-gjenbruksid", "**********");
    OslonokkelenClient client = new OslonokkelenClient(URI.create("ws://localhost:10000"), authClient, credentials, new AdapterController() {

      @Override
      public void start(LocalState state, ScheduledExecutorService executor) {
        System.out.println("Starting");
      }

      @Override
      public void onConnection(MessageSink messageSink) {
        System.out.println("Connected");
      }

      @Override
      public void onDisconnected() {
        System.out.println("Disconnected");
      }

      @Override
      public void ack(AckMessage ackMessage) {
        System.out.println("Ack: " + ackMessage);
      }

      @Override
      public void close() throws Exception {
        System.out.println("Closing..");
      }

    });


    client.start(new LocalState());
  }

}