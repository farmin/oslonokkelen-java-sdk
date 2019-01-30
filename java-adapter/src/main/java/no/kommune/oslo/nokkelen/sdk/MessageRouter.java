package no.kommune.oslo.nokkelen.sdk;

import no.kommune.oslo.nokkelen.api.data.AdapterThingDto;
import no.kommune.oslo.nokkelen.api.messages.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class MessageRouter {

  private final static Logger log = LoggerFactory.getLogger(MessageRouter.class);

  private final LocalState state;

  MessageRouter(LocalState state) {
    this.state = state;
  }

  /**
   * We are now connected to the backend!
   *
   * The first thing to do is to send the backend an
   * inventory of things attached to this adapter and
   * what those things can do.
   */
  void connected(MessageSink sink) {
    AdapterMessage reply = createThingDescriptionMessage();
    sink.sendMessage(reply);
  }

  private ThingsDefinitionMessage createThingDescriptionMessage() {
    Set<AdapterThingDto> things = state.stream()
            .map(LocalState.Thing::describe)
            .collect(Collectors.toSet());

    return new ThingsDefinitionMessage(things);
  }

  /**
   * The backend has asked us to execute a specific action
   * on a specific thing and we have to obey (or refuse)!
   *
   * @param sink Where to post replies
   * @param command The command to execute
   */
  void execute(MessageSink sink, ExecuteActionMessage command) {
    log.info("Executing {} requested by backend {}", command.getActionId(), command.getAppSourceId());

    LocalState.Thing thing = state.requireThing(command.getThingId());
    LocalState.Action action = thing.requireAction(command.getActionId());
    ActionRequest request = new ActionRequest(command.getUserParameters(), command.getActionParameters());

    try {
      ExecResult result = action.execute(request);
      ExecuteResponseMessage responseMessage = createResponseMessage(command, result.message(), result.status(), result.props());
      sink.sendMessage(responseMessage);
    }
    catch (Exception ex) {
      log.error("Failed executing {}", command, ex);
      ExecuteResponseMessage responseMessage = createResponseMessage(command, ex.getMessage(), ExecuteResponseMessage.Status.ERROR, Collections.emptyMap());
      sink.sendMessage(responseMessage);
    }
  }

  private ExecuteResponseMessage createResponseMessage(ExecuteActionMessage command, String message, ExecuteResponseMessage.Status status, Map<String, String> props) {
    return new ExecuteResponseMessage (
            command.getAppSourceId(),
            command.getRequestId(),
            props,
            message,
            status
    );
  }

  void ping(MessageSink sink, PingMessage message) {
    log.info("Got ping from server, responding med pong");
    AdapterMessage reply = new AdapterPongMessage(message.getServerTime(), "Alt vel!");
    sink.sendMessage(reply);
  }

}
