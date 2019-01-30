package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Message sent by the adapter in response to a ping.
 *
 * The adapter is supposed to copy the server time from the
 * ping message and use that in the pong. By doing that it
 * enables the backend to calculate the latency.
 */
public class AdapterPongMessage extends AdapterMessage {

  private final Instant serverTime;
  private final String status;

  @JsonCreator
  public AdapterPongMessage(@JsonProperty("serverTime") Instant serverTime, @JsonProperty("status") String status) {
    this.serverTime = serverTime;
    this.status = status;
  }

  public Instant getServerTime() {
    return serverTime;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return "Ponging ping sent at " + serverTime;
  }

  public boolean containsServerTime() {
    return serverTime != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AdapterPongMessage that = (AdapterPongMessage) o;
    return Objects.equals(serverTime, that.serverTime) &&
            Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverTime, status);
  }

}
