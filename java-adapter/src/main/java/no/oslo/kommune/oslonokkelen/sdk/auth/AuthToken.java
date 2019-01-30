package no.oslo.kommune.oslonokkelen.sdk.auth;

import java.net.URI;
import java.time.Instant;

public class AuthToken {

  private final String accessToken;
  private final String refreshToken;
  private final Instant refreshExpires;
  private final Instant accessExpiresIn;

  AuthToken(String accessToken, String refreshToken, Instant refreshExpires, Instant accessExpiresIn) {
    if (accessToken == null) {
      throw new IllegalArgumentException("Access token missing");
    }
    if (refreshToken == null) {
      throw new IllegalArgumentException("Refresh token missing");
    }

    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.refreshExpires = refreshExpires;
    this.accessExpiresIn = accessExpiresIn;
  }

  public String refreshToken() {
    return refreshToken;
  }

  public boolean hasExpiredRefreshToken() {
    return Instant.now().isAfter(refreshExpires);
  }

  public boolean hasExpiredAccessToken() {
    return Instant.now().isAfter(accessExpiresIn);
  }

  public URI accessEndpoint(URI baseUri) {
    return URI.create(baseUri + "?access_token=" + accessToken);
  }

  public String accessToken() {
    return accessToken;
  }

}
