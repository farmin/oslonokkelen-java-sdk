package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Initiated by the backend when it wants the adapter
 * to respond with a PongMessage.
 *
 * The adapter SHALL copy the serverTime into the pong
 * message. This is used to calculate round trip times.
 */
public class PingMessage extends AdapterMessage {

  private final Instant serverTime;

  @JsonCreator
  public PingMessage(@JsonProperty("serverTime") Instant serverTime) {
    if (serverTime == null) {
      throw new IllegalArgumentException("Server time is mandatory");
    }

    this.serverTime = serverTime;
  }

  public Instant getServerTime() {
    return serverTime;
  }

  @Override
  public String toString() {
    return "Please pong me";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PingMessage that = (PingMessage) o;
    return Objects.equals(serverTime, that.serverTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverTime);
  }

}
