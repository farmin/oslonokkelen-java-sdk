package no.kommune.oslo.nokkelen.sdk;

import java.util.Map;

/**
 * Represents a request to do something.
 */
public class ActionRequest {

  private final Map<String, String> userParameter;
  private final Map<String, String> actionParameters;

  ActionRequest(Map<String, String> userParameter, Map<String, String> actionParameters) {
    if (userParameter == null) {
      throw new IllegalArgumentException("User parameters can be empty, but not null");
    }
    if (actionParameters == null) {
      throw new IllegalArgumentException("Action parameters can be empty, but not null");
    }

    this.userParameter = userParameter;
    this.actionParameters = actionParameters;
  }

  /**
   * Require a named user parameter expected to be in the request.
   *
   * @param name Name of the user parameter
   * @return The associated value
   */
  public String userParameter(String name) {
    String value = userParameter.get(name);

    if (value == null) {
      throw new IllegalStateException("No user parameter: " + name);
    }

    return value;
  }

  /**
   * Require a named action parameter expected to be in the request.
   *
   * @param name Name of the action parameter
   * @return The associated value
   */
  public String actionParameter(String name) {
    String value = actionParameters.get(name);

    if (value == null) {
      throw new IllegalStateException("No action parameter: " + name);
    }

    return value;
  }

  public boolean hasUserParameter(String key) {
    return userParameter.containsKey(key);
  }

}
