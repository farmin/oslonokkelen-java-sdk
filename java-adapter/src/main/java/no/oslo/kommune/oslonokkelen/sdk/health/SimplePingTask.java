package no.oslo.kommune.oslonokkelen.sdk.health;

import no.oslo.kommune.oslonokkelen.adapter.messages.AdapterHealthMessage;
import no.oslo.kommune.oslonokkelen.sdk.LocalState;
import no.oslo.kommune.oslonokkelen.sdk.MessageSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * A skeleton for a really simple health check that..
 *
 *    1. Polls an external service
 *    2. Uses the result to update our local view of the world
 *    3. Sends an updated health report to our backend
 */
public abstract class SimplePingTask extends MessageTask {

  private final static Logger log = LoggerFactory.getLogger(SimplePingTask.class);

  private final LocalState state;

  public SimplePingTask(AtomicReference<MessageSink> sink, LocalState state) {
    super(sink);
    this.state = state;
  }

  @Override
  public final void run() {
    if (isDisconnected()) {
      log.debug("Skipping health check due to missing backend connection");
      return;
    }

    updateActionHealth(state);
    reportHealth();
  }

  protected abstract void updateActionHealth(LocalState state);

  private void reportHealth() {
    MessageSink messageSink = this.sink.get();

    if (messageSink != null) {
      try {
        AdapterHealthMessage healthMessage = state.createHealthMessage();
        messageSink.sendMessage(healthMessage);
      }
      catch (Exception ex) {
        log.warn("Failed to send health report", ex);
      }
    }
  }

}
