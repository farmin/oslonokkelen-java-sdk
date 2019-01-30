package no.kommune.oslo.nokkelen.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Description of something that can be done to a thing
 * controlled by an adapter somewhere..
 */
public class AdapterActionDto extends IdentifiedDto {

  private final Set<String> userParameters;
  private final Set<String> actionParameters;

  private String healthMessage;
  private AdapterHealthStatusDto healthStatus;
  private Instant healthUpdate;

  @JsonCreator
  public AdapterActionDto(@JsonProperty("id") String id,
                          @JsonProperty("userParameters") Set<String> userParameters,
                          @JsonProperty("actionParameters") Set<String> actionParameters,
                          @JsonProperty("healthMessage")  String healthMessage,
                          @JsonProperty("healthStatus")  AdapterHealthStatusDto healthStatus) {

    super(id);
    this.userParameters = userParameters == null ? Collections.emptySet() : userParameters;
    this.actionParameters = actionParameters == null ? Collections.emptySet() : actionParameters;
    this.healthStatus = healthStatus == null ? AdapterHealthStatusDto.UNKNOWN : healthStatus;
    this.healthMessage = healthMessage;
  }



  public Set<String> getUserParameters() {
    return userParameters;
  }

  public Set<String> getActionParameters() {
    return actionParameters;
  }

  public String getHealthMessage() {
    return healthMessage;
  }

  public AdapterHealthStatusDto getHealthStatus() {
    return healthStatus;
  }

  public Instant getHealthUpdate() {
    return healthUpdate;
  }

  public static Builder builder(String id) {
    return new Builder(id);
  }

  @Override
  public String toString() {
    return String.format("Action: %s, required user parameters: %s, required action parameters: %s", id, userParameters, actionParameters);
  }

  public void updateFrom(ThingHealthDto.Op update) {
    healthStatus = update.getStatus();
    healthMessage = update.getMessage();
    healthUpdate = Instant.now();
  }

  public static class Builder {

    private final String id;
    private final Set<String> actionParameters = new HashSet<>();
    private final Set<String> userParameters = new HashSet<>();

    Builder(String id) {
      this.id = id;
    }

    public Builder requiredUserParameter(String name) {
      this.userParameters.add(name);
      return this;
    }

    public Builder requiredActionParameter(String name) {
      this.actionParameters.add(name);
      return this;
    }

    AdapterActionDto done() {
      return new AdapterActionDto(id, userParameters, actionParameters, null, AdapterHealthStatusDto.UNKNOWN);
    }

    public Builder requiredActionParameters(Set<String> params) {
      this.actionParameters.addAll(params);
      return this;
    }

    public Builder requiredUserParameters(Set<String> params) {
      this.userParameters.addAll(params);
      return this;
    }
  }

}
