package no.kommune.oslo.nokkelen.sdk;

import no.kommune.oslo.nokkelen.api.data.AdapterHealthStatusDto;
import no.kommune.oslo.nokkelen.api.data.AdapterThingDto;
import no.kommune.oslo.nokkelen.api.data.ThingHealthDto;
import no.kommune.oslo.nokkelen.api.messages.AdapterHealthMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains a list of all described things etc..
 */
public class LocalState implements Iterable<LocalState.Thing> {

  private final static Logger log = LoggerFactory.getLogger(LocalState.class);

  private final List<Thing> things = new CopyOnWriteArrayList<>();

  private final AtomicInteger version = new AtomicInteger(0);

  public Thing addThing(String id, Consumer<ThingBuilder> config) {
    log.debug("Adding {}", id);

    ThingBuilder tb = new ThingBuilder(id);
    config.accept(tb);
    Thing thing = tb.build();
    things.add(thing);
    return thing;
  }

  public void removeThing(String id) {
    log.debug("Removing {}", id);
    things.removeIf(t -> t.id.equals(id));
  }

  public Thing requireThing(String id) {
    Optional<Thing> result = findThingById(id);

    if (result.isPresent()) {
      return result.get();
    }
    else {
      throw new IllegalStateException("No thing with id " + id);
    }
  }

  public Optional<Thing> findThingById(String id) {
    return things.stream()
              .filter(t -> t.id.equals(id))
              .findFirst();
  }

  @Override
  public Iterator<Thing> iterator() {
    return things.iterator();
  }

  public Stream<Thing> stream() {
    return things.stream();
  }

  /**
   * Flag that we've changed something.
   * This is a really stupid implementation, but it will have to do for now
   */
  public void touch() {
    version.incrementAndGet();
  }

  public AdapterHealthMessage createHealthMessage() {
    Set<ThingHealthDto> t = things.stream()
            .map(thing -> {
              Set<ThingHealthDto.Op> ops = thing.actions.stream()
                      .map(o -> new ThingHealthDto.Op(o.id(), o.healthStatus, o.healthMessage))
                      .collect(Collectors.toSet());

              return new ThingHealthDto(thing.id(), ops);
            })
            .collect(Collectors.toSet());

    return new AdapterHealthMessage("health/" + UUID.randomUUID().toString(), t);
  }


  public static class ThingBuilder {

    private final Thing thing;

    ThingBuilder(String id) {
      thing = new Thing(id);
    }

    /**
     * A thermometer thing might have temperature and humidity props.
     *
     * @param props properties associated with the thing
     * @return the build
     */
    public ThingBuilder readableProperties(String... props) {
      thing.properties.addAll(Arrays.asList(props));
      return this;
    }


    private Thing build() {
      return thing;
    }

    public void action(String id, Function<ActionBuilder, ActionDelegate> config) {
      ActionBuilder ob = new ActionBuilder(id);
      ActionDelegate exec = config.apply(ob);
      Action action = ob.build(exec);
      thing.actions.add(action);
    }

  }

  @FunctionalInterface
  public interface ActionDelegate {

    ExecResult execute(ActionRequest request) throws Exception;

  }


  public static class ActionBuilder {


    private final Action action;

    ActionBuilder(String id) {
      this.action = new Action(id);
    }

    /**
     * Stuff we need to know about the user executing the action
     * @param props For example username, email..
     * @return the builder
     */
    public ActionBuilder requiredUserParameters(String... props) {
      action.userParameters.addAll(Arrays.asList(props));
      return this;
    }

    /**
     * Stuff we need to know about the action being executed
     * @param props For dim the light to x %
     * @return the builder
     */
    public ActionBuilder requiredActionParameters(String... props) {
      action.actionParameters.addAll(Arrays.asList(props));
      return this;
    }

    private Action build(ActionDelegate exec) {
      action.exec = exec;
      return action;
    }

    /**
     *
     * @param description Human readable description
     */
    public ActionBuilder describe(String description, Object... args) {
      action.description = String.format(description, args);
      return this;
    }
  }

  public static class Thing extends Base {

    private final Set<Action> actions = new TreeSet<>();

    private final Set<String> properties = new TreeSet<>();

    Thing(String id) {
      super(id);
    }

    public Action requireAction(String id) {
      Optional<Action> result = actions.stream()
              .filter(o -> o.id.equals(id))
              .findFirst();

      if (result.isPresent()) {
        return result.get();
      }
      else {
        throw new IllegalStateException("No action in thing " + id + " with id " + id);
      }
    }

    AdapterThingDto describe() {
      AdapterThingDto.Builder builder = AdapterThingDto.builder(id);

      for (String property : properties) {
        builder.readableProp(property);
      }
      for (Action action : actions) {
        builder.supportedAction(action.id, (ob) -> {
          ob.requiredActionParameters(action.actionParameters);
          ob.requiredUserParameters(action.userParameters);
        });
      }

      return builder.done();
    }

  }


  public static class Action extends Base {

    private final Set<String> userParameters = new TreeSet<>();
    private final Set<String> actionParameters = new TreeSet<>();

    String description;

    private ActionDelegate exec;

    private AdapterHealthStatusDto healthStatus = AdapterHealthStatusDto.UNKNOWN;
    private String healthMessage = "No report yet";

    Action(String id) {
      super(id);
    }

    public Action userParameters(String... userParameters) {
      this.userParameters.addAll(Arrays.asList(userParameters));
      return this;
    }

    public Action actionParameters(String... actionParameters) {
      this.actionParameters.addAll(Arrays.asList(actionParameters));
      return this;
    }

    ExecResult execute(ActionRequest request) throws Exception {
      return exec.execute(request);
    }

    public void reportWorking(String message, Object... args) {
      updateHealth(AdapterHealthStatusDto.WORKING, message, args);
    }

    public void reportBroken(String message, Object... args) {
      updateHealth(AdapterHealthStatusDto.BROKEN, message, args);
    }

    private void updateHealth(AdapterHealthStatusDto status, String message, Object[] args) {
      healthMessage = String.format(message, args);
      healthStatus = status;
    }

  }



  static abstract class Base implements Comparable<Base> {

    final String id;

    Base(String id) {
      this.id = id;
    }

    public String id() {
      return id;
    }

    @Override
    public final int compareTo(Base o) {
      return id.compareTo(o.id);
    }

    @Override
    public final boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Base ab = (Base) o;
      return id.equals(ab.id);
    }

    @Override
    public final int hashCode() {
      return Objects.hash(id);
    }

  }

}
