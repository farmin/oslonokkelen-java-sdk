package no.kommune.oslo.nokkelen.sdk.auth;

import java.util.Objects;

public class ClientCredentials {

  private final String id;
  private final String secret;

  public ClientCredentials(String id, String secret) {
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("Client id is missing");
    }
    if (secret == null || secret.isEmpty()) {
      throw new IllegalArgumentException("Secret is missing");
    }

    this.secret = secret;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ClientCredentials that = (ClientCredentials) o;
    return id.equals(that.id) &&
            secret.equals(that.secret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, secret);
  }

  @Override
  public String toString() {
    return id + ":**************";
  }

  String id() {
    return id;
  }

  String secret() {
    return secret;
  }

}
