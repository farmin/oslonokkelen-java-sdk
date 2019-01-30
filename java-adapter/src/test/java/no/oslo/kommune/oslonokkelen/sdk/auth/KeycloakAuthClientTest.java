package no.oslo.kommune.oslonokkelen.sdk.auth;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

@Disabled("For local testing")
class KeycloakAuthClientTest {

  @Test
  @DisplayName("test")
  void test() {
    try (KeycloakAuthClient client = new KeycloakAuthClient(URI.create("http://keycloak:8080"), "oslonokkelen")) {
      ClientCredentials credentials = new ClientCredentials("adapter-bitraf", "e16da4da-da2c-48a3-8993-39fb9efffedd");
      AuthToken token = client.authenticate(credentials);
      AuthToken refreshed = client.refresh(credentials, token);

      client.logout(credentials, refreshed);
    }
  }

}