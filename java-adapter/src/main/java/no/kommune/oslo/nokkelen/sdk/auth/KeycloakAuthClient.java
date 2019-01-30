package no.kommune.oslo.nokkelen.sdk.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.Instant;

import static java.util.concurrent.TimeUnit.SECONDS;

public class KeycloakAuthClient implements AuthClient {

  private final static Logger log = LoggerFactory.getLogger(KeycloakAuthClient.class);

  private final Client client;

  private final WebTarget authTarget;
  private final WebTarget logoutTarget;

  public KeycloakAuthClient(URI keycloakBaseURI, String realm) {
    client = ClientBuilder.newBuilder()
            .connectTimeout(1, SECONDS)
            .readTimeout(3, SECONDS)
            .build();

    authTarget = client.target(String.format("%s/auth/realms/%s/protocol/openid-connect/token", keycloakBaseURI, realm));
    logoutTarget = client.target(String.format("%s/auth/realms/%s/protocol/openid-connect/logout", keycloakBaseURI, realm));
  }

  @Override
  public AuthToken authenticate(ClientCredentials credentials) {
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("grant_type", "client_credentials");
    formData.add("client_id", credentials.id());
    formData.add("client_secret", credentials.secret());

    return requestTokens(formData);
  }

  @Override
  public AuthToken refresh(ClientCredentials credentials, AuthToken oldToken) {
    if (oldToken.hasExpiredRefreshToken()) {
      log.debug("Refresh token has expired, doing a new authentication");
      return authenticate(credentials);
    }
    else {
      MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
      formData.add("grant_type", "refresh_token");
      formData.add("client_id", credentials.id());
      formData.add("client_secret", credentials.secret());
      formData.add("refresh_token", oldToken.refreshToken());

      return requestTokens(formData);
    }
  }

  protected AuthToken requestTokens(MultivaluedMap<String, String> formData) {
    try (Response response = authTarget.request().post(Entity.form(formData))) {
      if (response.getStatus() != 200) {
        String message = String.format("Unable to get tokens from %s: %s", authTarget.getUri(), response.getStatusInfo());
        throw new IllegalStateException(message);
      }

      ObjectNode entity = response.readEntity(ObjectNode.class);

      String accessToken = entity.get("access_token").asText();
      String refreshToken = entity.get("refresh_token").asText();
      long refreshExpires = entity.get("refresh_expires_in").asLong();
      long accessExpires = entity.get("expires_in").asLong();

      return new AuthToken(accessToken, refreshToken, Instant.now().plusSeconds((long) (refreshExpires * 0.9)), Instant.now().plusSeconds((long) (accessExpires * 0.9)));
    }
  }

  @Override
  public void logout(ClientCredentials credentials, AuthToken currentToken) {
    MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
    formData.add("client_id", credentials.id());
    formData.add("client_secret", credentials.secret());
    formData.add("refresh_token", currentToken.refreshToken());

    invokeLogoutTarget(formData);
  }

  protected void invokeLogoutTarget(MultivaluedMap<String, String> formData) {
    try (Response ignored = logoutTarget.request().post(Entity.form(formData))) {
      log.debug("Logged out");
    }
  }

  @Override
  public void close() {
    log.debug("Closing keycloak client");
    client.close();
  }

}
