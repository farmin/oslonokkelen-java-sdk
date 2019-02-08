package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class ExecuteResponseMessage extends AdapterMessage {

  /**
   * Copy this from the incoming message.
   */
  private final String appSourceId;

  /**
   * Copy this from the incoming message.
   */
  private final String requestId;

  private final Status status;

  /**
   * This code will be translated into a human
   * readable message.
   */
  private final String errorCode;

  /**
   * Can be used for communicating a technical description
   * of why something went wrong. It will not be sent to the end user.
   */
  private final String message;

  private final Map<String, String> properties;

  @JsonCreator
  public ExecuteResponseMessage(@JsonProperty("appSourceId") String appSourceId,
                                @JsonProperty("requestId") String requestId,
                                @JsonProperty("properties") Map<String, String> properties,
                                @JsonProperty("message") String message,
                                @JsonProperty("status") Status status,
                                @JsonProperty("errorCode") String errorCode) {

    if (requestId == null) {
      throw new IllegalArgumentException("Original request id is mandatory");
    }

    this.properties = properties;
    this.appSourceId = appSourceId;
    this.errorCode = errorCode;
    this.requestId = requestId;
    this.message = message;
    this.status = status;
  }

  public String getAppSourceId() {
    return appSourceId;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getMessage() {
    return message;
  }

  public Status getStatus() {
    return status;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String toString() {
    return String.format("Reply to execute request %s: %s", requestId, status);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExecuteResponseMessage that = (ExecuteResponseMessage) o;
    return Objects.equals(appSourceId, that.appSourceId) &&
        Objects.equals(requestId, that.requestId) &&
        status == that.status &&
        Objects.equals(errorCode, that.errorCode) &&
        Objects.equals(message, that.message) &&
        Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(appSourceId, requestId, status, errorCode, message, properties);
  }

  public enum Status {

    /** Everything went well */
    OK,

    /** Something is really wrong, no need trying again */
    ERROR,

    /** Something didn't go as planned, but it might be temporary. Feel free to try again. */
    HICCUP,

    /** User was denied access. No ened trying again. */
    ACCESS_DENIED

  }

}
