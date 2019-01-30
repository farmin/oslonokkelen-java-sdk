package no.kommune.oslo.nokkelen.sdk.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthTokenTest {

  @Test
  @DisplayName("Create URI")
  void create_uri() {
    AuthToken token = new AuthToken("access", "refresh", Instant.MAX, Instant.MAX);
    URI uri = token.accessEndpoint(URI.create("wss://on.no"));
    URI expected = URI.create("wss://on.no?access_token=access");

    assertEquals(expected, uri);
  }

}