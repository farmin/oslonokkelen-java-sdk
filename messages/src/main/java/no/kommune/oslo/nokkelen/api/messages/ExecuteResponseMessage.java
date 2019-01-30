package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

public class ExecuteResponseMessage extends AdapterMessage {

  private final String appSourceId;
  private final String requestId;
  private final String message;
  private final Status status;
  private final Map<String, String> properties;

  @JsonCreator
  public ExecuteResponseMessage(@JsonProperty("appSourceId") String appSourceId,
                                @JsonProperty("requestId") String requestId,
                                @JsonProperty("properties") Map<String, String> properties,
                                @JsonProperty("message") String message,
                                @JsonProperty("status") Status status) {

    if (requestId == null) {
      throw new IllegalArgumentException("Original request id is mandatory");
    }

    this.properties = properties;
    this.appSourceId = appSourceId;
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
            Objects.equals(message, that.message) &&
            status == that.status;
  }

  @Override
  public int hashCode() {
    return Objects.hash(appSourceId, requestId, message, status);
  }

  public enum Status {

    OK,

    ERROR,

    ACCESS_DENIED

  }

}
