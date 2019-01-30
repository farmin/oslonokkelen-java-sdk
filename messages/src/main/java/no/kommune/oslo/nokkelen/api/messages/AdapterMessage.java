package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

/**
 * Represents a message to or from an adapter.
 */
@JsonSubTypes({

        @JsonSubTypes.Type(value = AdapterErrorMessage.class, name = "error"),
        @JsonSubTypes.Type(value = AdapterHealthMessage.class, name = "health"),

        @JsonSubTypes.Type(value = PingMessage.class, name = "ping"),
        @JsonSubTypes.Type(value = AdapterPongMessage.class, name = "pong"),

        @JsonSubTypes.Type(value = ExecuteActionMessage.class, name = "execute-action"),
        @JsonSubTypes.Type(value = ExecuteResponseMessage.class, name = "execute-response"),

        @JsonSubTypes.Type(value = ThingsDefinitionMessage.class, name = "things-description"),

        @JsonSubTypes.Type(value = PropertiesChangeMessage.class, name = "properties-change"),
        @JsonSubTypes.Type(value = AckMessage.class, name = "ack")

})
@JsonTypeInfo(use = NAME, property = "type")
public abstract class AdapterMessage {

  private final String messageId;

  protected AdapterMessage() {
    messageId = UUID.randomUUID().toString();
  }

  protected AdapterMessage(String messageId) {
    this.messageId = messageId;
  }

  public String getMessageId() {
    return messageId;
  }

  @Override
  public String toString() {
    return "Message: " + messageId;
  }

}
