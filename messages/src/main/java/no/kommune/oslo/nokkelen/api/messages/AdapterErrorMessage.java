package no.kommune.oslo.nokkelen.api.messages;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

/**
 * This message is used to inform the adapter that it has
 * done something wrong. Depending on the severity it might
 * be followed by a disconnect.
 */
public class AdapterErrorMessage extends AdapterMessage {

  private final String message;
  private final boolean willTerminate;

  /**
   *
   * @param message With information to the adapter
   * @param willTerminate Will be true if the error is severe enough to terminate the connection
   */
  @JsonCreator
  public AdapterErrorMessage(String message, boolean willTerminate) {
    this.message = message;
    this.willTerminate = willTerminate;
  }

  public static AdapterErrorMessage severeError(String message) {
    return new AdapterErrorMessage(message, true);
  }

  public static AdapterErrorMessage minorError(String message) {
    return new AdapterErrorMessage(message, false);
  }

  public String getMessage() {
    return message;
  }

  public boolean isWillTerminate() {
    return willTerminate;
  }

  @Override
  public String toString() {
    if (willTerminate) {
      return "Fatal error: " + message;
    }
    else {
      return "Minor error: " + message;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AdapterErrorMessage that = (AdapterErrorMessage) o;
    return willTerminate == that.willTerminate &&
            Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, willTerminate);
  }

}
