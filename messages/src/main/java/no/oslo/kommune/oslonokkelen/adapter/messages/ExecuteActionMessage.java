package no.oslo.kommune.oslonokkelen.adapter.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Objects;

/**
 * The adapter is requested to execute an actionId.
 */
public class ExecuteActionMessage extends AdapterMessage {

  /**
   * A unique id identifying this request.
   * Without this we won't be able to correlate the
   * adapter response with the app waiting for it.
   *
   * The adapter must copy this when replying.
   */
  private final String requestId;


  /**
   * A id identifying the thingId connected to the adapter.
   */
  private final String thingId;

  /**
   * A id identifying the operationId available on the thingId
   */
  private final String actionId;

  /**
   * A unique id of the backend that made the request.
   * For now this is needed to correlate the adapter response
   * with the backend waiting for the response. In the future
   * it would be nice if the backend responsible for the
   * adapter could keep this state.
   */
  private final String appSourceId;

  /**
   * Data about the user requested by the adapter.
   * For example name or email address.
   */
  private final Map<String, String> userParameters;

  /**
   * Parameters for the operationId to be executed.
   * For example: Dim light to 40%
   */
  private final Map<String, String> actionParameters;

  @JsonCreator
  public ExecuteActionMessage(@JsonProperty("appSourceId") String appSourceId,
                              @JsonProperty("requestId") String requestId,
                              @JsonProperty("thingId") String thingId,
                              @JsonProperty("actionId") String actionId,
                              @JsonProperty("userParameters") Map<String, String> userParameters,
                              @JsonProperty("actionParameters") Map<String, String> actionParameters) {

    this.requestId = requestId;
    this.appSourceId = appSourceId;
    this.thingId = thingId;
    this.actionId = actionId;
    this.userParameters = userParameters;
    this.actionParameters = actionParameters;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getThingId() {
    return thingId;
  }

  public String getActionId() {
    return actionId;
  }

  public String getAppSourceId() {
    return appSourceId;
  }

  public Map<String, String> getUserParameters() {
    return userParameters;
  }

  public Map<String, String> getActionParameters() {
    return actionParameters;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ExecuteActionMessage that = (ExecuteActionMessage) o;
    return Objects.equals(requestId, that.requestId) &&
            Objects.equals(thingId, that.thingId) &&
            Objects.equals(actionId, that.actionId) &&
            Objects.equals(appSourceId, that.appSourceId) &&
            Objects.equals(userParameters, that.userParameters) &&
            Objects.equals(actionParameters, that.actionParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, thingId, actionId, appSourceId, userParameters, actionParameters);
  }

  @Override
  public String toString() {
    return String.format("Action request %s for %s", requestId, actionId);
  }

}
