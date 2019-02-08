package no.kommune.oslo.nokkelen.sdk;


import no.kommune.oslo.nokkelen.api.messages.ExecuteResponseMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExecResult {

  private final ExecuteResponseMessage.Status status;
  private final String message;

  private final Map<String, String> properties = new HashMap<>();
  private final String errorCode;

  public ExecResult(ExecuteResponseMessage.Status status, String errorCode, String message) {
    this.errorCode = errorCode;
    if (status == null) {
      throw new IllegalArgumentException("Status is mandatory");
    }

    this.message = message;
    this.status = status;
  }

  public static ExecResult success(String message) {
    return new ExecResult(ExecuteResponseMessage.Status.OK, null, message);
  }

  public static ExecResult error(String errorCode, String message) {
    return new ExecResult(ExecuteResponseMessage.Status.ERROR, errorCode, message);
  }

  public static ExecResult accessDenied(String errorCode, String message) {
    return new ExecResult(ExecuteResponseMessage.Status.ACCESS_DENIED, errorCode, message);
  }

  public static ExecResult hiccup(String errorCode, String message) {
    return new ExecResult(ExecuteResponseMessage.Status.HICCUP, errorCode, message);
  }



  public ExecResult withProperty(String key, String value) {
    this.properties.put(key, value);
    return this;
  }

  ExecuteResponseMessage.Status status() {
    return status;
  }

  String message() {
    return message;
  }

  @Override
  public String toString() {
    return status + ": " + message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExecResult that = (ExecResult) o;
    return status == that.status &&
            Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, message);
  }

  public Map<String, String> props() {
    return properties;
  }

  public String errorCode() {
    return errorCode;
  }
}
