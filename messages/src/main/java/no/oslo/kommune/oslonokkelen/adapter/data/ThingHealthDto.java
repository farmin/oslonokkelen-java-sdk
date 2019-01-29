package no.oslo.kommune.oslonokkelen.adapter.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public class ThingHealthDto extends IdentifiedDto {

  private final Set<Op> actions;

  @JsonCreator
  public ThingHealthDto(@JsonProperty("id") String id, @JsonProperty("actions") Set<Op> actions) {
    super(id);
    this.actions = actions;
  }

  public Set<Op> getActions() {
    return actions;
  }

  @Override
  public String toString() {
    return id + ": " + actions;
  }


  public static class Op extends IdentifiedDto {

    private final AdapterHealthStatusDto status;
    private final String message;

    @JsonCreator
    public Op(@JsonProperty("id") String id,
              @JsonProperty("status") AdapterHealthStatusDto status,
              @JsonProperty("message") String message) {

      super(id);
      this.status = status;
      this.message = message;
    }

    public AdapterHealthStatusDto getStatus() {
      return status;
    }

    public String getMessage() {
      return message;
    }

    @Override
    public String toString() {
      return id + ": " + status + (message != null ? " (message: " + message + ")" : "(no message)");
    }

  }

}



