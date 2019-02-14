package no.kommune.oslo.nokkelen.sdk.auth;


import com.fasterxml.jackson.databind.JsonNode;
import no.kommune.oslo.nokkelen.sdk.serialization.JsonSerializer;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

public class KeycloakAuthClient implements AuthClient {

  private final static Logger log = LoggerFactory.getLogger(KeycloakAuthClient.class);

  private final OkHttpClient client;

  private final URL authURI;
  private final URL logoutURI;

  public KeycloakAuthClient(URI keycloakBaseURI, String realm) {
    client = new OkHttpClient.Builder()
        .connectTimeout(1, SECONDS)
        .readTimeout(4, SECONDS)
        .writeTimeout(1, SECONDS)
        .callTimeout(10, SECONDS)
        .retryOnConnectionFailure(true)
        .build();

    authURI = createUrl(keycloakBaseURI, realm, "%s/auth/realms/%s/protocol/openid-connect/token");
    logoutURI = createUrl(keycloakBaseURI, realm, "%s/auth/realms/%s/protocol/openid-connect/logout");
  }

  private URL createUrl(URI base, String realm, String path) {
    try {
      return URI.create(String.format(path, base, realm)).toURL();
    }
    catch (MalformedURLException e) {
      throw new IllegalStateException("Failed to create url: " + path);
    }
  }

  @Override
  public AuthToken authenticate(ClientCredentials credentials) {
    Map<String, String> formData = new HashMap<>();
    formData.put("grant_type", "client_credentials");
    formData.put("client_id", credentials.id());
    formData.put("client_secret", credentials.secret());

    return requestTokens(formData);
  }

  @Override
  public AuthToken refresh(ClientCredentials credentials, AuthToken oldToken) {
    if (oldToken.hasExpiredRefreshToken()) {
      log.debug("Refresh token has expired, doing a new authentication");
      return authenticate(credentials);
    }
    else {
      Map<String, String> formData = new HashMap<>();
      formData.put("grant_type", "refresh_token");
      formData.put("client_id", credentials.id());
      formData.put("client_secret", credentials.secret());
      formData.put("refresh_token", oldToken.refreshToken());

      return requestTokens(formData);
    }
  }

  private AuthToken requestTokens(Map<String, String> formData) {
    Request request = new Request.Builder()
        .url(authURI)
        .post(withForm(formData))
        .build();

    try {
      Response response = client.newCall(request).execute();
      if (!response.isSuccessful()) {
        String message = String.format("Unable to get tokens from %s (http: %s): %s", authURI, response.code(), response.body());
        throw new IllegalStateException(message);
      }

      JsonNode json = readJsonResponse(response);

      String accessToken = json.get("access_token").textValue();
      String refreshToken = json.get("refresh_token").textValue();
      long refreshExpires = json.get("refresh_expires_in").longValue();
      long accessExpires = json.get("expires_in").longValue();

      return new AuthToken(accessToken, refreshToken, Instant.now().plusSeconds((long) (refreshExpires * 0.9)), Instant.now().plusSeconds((long) (accessExpires * 0.9)));
    }
    catch (Exception ex) {
      throw new IllegalStateException("Failed to log in", ex);
    }
  }

  private JsonNode readJsonResponse(Response response) throws IOException {
    ResponseBody body = response.body();

    if (body != null) {
      String str = body.string();
      return JsonSerializer.readTree(str);
    }
    else {
      throw new IllegalStateException("No response body: " + response);
    }
  }

  private FormBody withForm(Map<String, String> formData) {
    FormBody.Builder requestBuilder = new FormBody.Builder();

    for (String key : formData.keySet()) {
      requestBuilder.add(key, formData.get(key));
    }

    return requestBuilder.build();
  }

  @Override
  public void logout(ClientCredentials credentials, AuthToken currentToken) {
    Map<String, String> formData = new HashMap<>();
    formData.put("client_id", credentials.id());
    formData.put("client_secret", credentials.secret());
    formData.put("refresh_token", currentToken.refreshToken());

    invokeLogoutTarget(formData);
  }

  private void invokeLogoutTarget(Map<String, String> formData) {
    Request request = new Request.Builder()
        .url(logoutURI)
        .post(withForm(formData))
        .build();

    try (Response response = client.newCall(request).execute()) {
      System.out.println(response);
    }
    catch (Exception ex) {
      throw new IllegalStateException("Failed to log out", ex);
    }
  }

  @Override
  public void close() {
    log.info("Shutting down http client");
    client.dispatcher().executorService().shutdown();
    client.connectionPool().evictAll();
  }

}
