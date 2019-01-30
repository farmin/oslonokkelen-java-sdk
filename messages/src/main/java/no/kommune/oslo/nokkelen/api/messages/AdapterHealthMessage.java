package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.kommune.oslo.nokkelen.api.data.ThingHealthDto;

import java.util.Objects;
import java.util.Set;

/**
 * The adapter can send this message whenever it wants to
 * update the backend with the latest health report.
 */
public class AdapterHealthMessage extends AdapterMessage {

  private final Set<ThingHealthDto> things;

  @JsonCreator
  public AdapterHealthMessage(@JsonProperty("messageId")  String messageId, @JsonProperty("things") Set<ThingHealthDto> things) {
    super(messageId);
    this.things = things;
  }

  public Set<ThingHealthDto> getThings() {
    return things;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AdapterHealthMessage that = (AdapterHealthMessage) o;
    return Objects.equals(things, that.things);
  }

  @Override
  public int hashCode() {
    return Objects.hash(things);
  }

  @Override
  public String toString() {
    return "Health status report: " + things;
  }

}
