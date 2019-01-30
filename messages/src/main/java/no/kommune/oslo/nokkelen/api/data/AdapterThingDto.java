package no.kommune.oslo.nokkelen.api.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.function.Consumer;

/**
 * Description of a thing that is controllable by a certain adapter.
 */
public class AdapterThingDto extends IdentifiedDto {

  private final Map<String, String> properties;
  private final Set<AdapterActionDto> actions;

  @JsonCreator
  public AdapterThingDto(@JsonProperty("id") String id,
                         @JsonProperty("properties") Map<String, String> properties,
                         @JsonProperty("actions") Set<AdapterActionDto> actions) {

    super(id);

    this.properties = properties;
    this.actions = actions;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public Set<AdapterActionDto> getActions() {
    return actions;
  }

  public static Builder builder(String id) {
    return new Builder(id);
  }

  public AdapterActionDto requireAction(String id) {
    return findAction(id).orElseThrow(() -> new IllegalStateException("No action with id: "+ id));
  }

  public Optional<AdapterActionDto> findAction(String id) {
    return actions.stream()
            .filter(o -> o.id.equals(id))
            .findFirst();
  }

  public boolean hasAction(String id) {
    return actions.stream().anyMatch(a -> a.id.equals(id));
  }


  public static class Builder {

    private final String id;
    private final Set<AdapterActionDto> ops = new HashSet<>();
    private final Map<String, String> props = new HashMap<>();

    Builder(String id) {
      this.id = id;
    }

    public Builder readableProp(String name) {
      return readableProp(name, null);
    }

    public Builder readableProp(String name, String value) {
      this.props.put(name, value);
      return this;
    }

    public Builder supportedAction(String id, Consumer<AdapterActionDto.Builder> consumer) {
      AdapterActionDto.Builder builder = AdapterActionDto.builder(id);
      consumer.accept(builder);
      AdapterActionDto op = builder.done();
      ops.add(op);
      return this;
    }

    public AdapterThingDto done() {
      return new AdapterThingDto(id, props, ops);
    }

  }

}
