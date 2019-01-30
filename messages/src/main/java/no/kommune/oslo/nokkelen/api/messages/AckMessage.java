package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The adapter can acknowledge that a message has been
 * received _and_ acted upon by sending this message in return.
 */
public class AckMessage extends AdapterMessage {

  private final String acknowledgedMessageId;

  @JsonCreator
  public AckMessage(@JsonProperty("acknowledgedMessageId") String acknowledgedMessageId) {
    this.acknowledgedMessageId = acknowledgedMessageId;
  }

  public String getAcknowledgedMessageId() {
    return acknowledgedMessageId;
  }

  @Override
  public String toString() {
    return "Ack for message " + acknowledgedMessageId;
  }

}
