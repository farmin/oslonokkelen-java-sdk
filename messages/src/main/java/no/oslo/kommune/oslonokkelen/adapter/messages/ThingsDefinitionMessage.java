package no.oslo.kommune.oslonokkelen.adapter.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.oslo.kommune.oslonokkelen.adapter.data.AdapterThingDto;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A thing will always have an id. In addition to the id
 * it has a number of properties (temperature, battery level..)
 * plus a number of supported actions like open door,
 * program pin code..
 *
 * Some actions might require additional parameters.
 * Programming a lock with a pin code will for example
 * require a pin code parameter..
 */
public class ThingsDefinitionMessage extends AdapterMessage {

  private final Set<AdapterThingDto> things;

  @JsonCreator
  public ThingsDefinitionMessage(@JsonProperty("things") Set<AdapterThingDto> things) {
    if (things == null) {
      throw new IllegalArgumentException("Things are mandatory");
    }

    this.things = things;
  }

  public Set<AdapterThingDto> getThings() {
    return things;
  }

  @Override
  public String toString() {
    return String.format("Things: %s", things);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ThingsDefinitionMessage that = (ThingsDefinitionMessage) o;
    return Objects.equals(things, that.things);
  }

  @Override
  public int hashCode() {
    return Objects.hash(things);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final Set<AdapterThingDto> things;

    Builder() {
      things = new HashSet<>();
    }

    public Builder defineThing(String id, Consumer<AdapterThingDto.Builder> consumer) {
      AdapterThingDto.Builder builder = AdapterThingDto.builder(id);
      consumer.accept(builder);
      things.add(builder.done());
      return this;
    }

    public ThingsDefinitionMessage done() {
      return new ThingsDefinitionMessage(things);
    }

  }


}
