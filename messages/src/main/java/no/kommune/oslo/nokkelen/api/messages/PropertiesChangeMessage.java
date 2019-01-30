package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * The client MAY send this message whenever a property has changed.
 *
 * Examples:
 *
 *  - The door is now open
 *  - The temperature is x degrees
 *  - Tampering detected
 */
public class PropertiesChangeMessage extends AdapterMessage {

  private final String thingId;

  private final Map<String, String> properties;

  @JsonCreator
  public PropertiesChangeMessage(@JsonProperty("thingId") String thingId,
                                 @JsonProperty("properties") Map<String, String> properties) {

    this.thingId = thingId;
    this.properties = properties;
  }

  public String getThingId() {
    return thingId;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PropertiesChangeMessage that = (PropertiesChangeMessage) o;
    return Objects.equals(thingId, that.thingId) &&
            Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(thingId, properties);
  }

  @Override
  public String toString() {
    return "Changed properties for " + thingId + ": " + properties;
  }

}
