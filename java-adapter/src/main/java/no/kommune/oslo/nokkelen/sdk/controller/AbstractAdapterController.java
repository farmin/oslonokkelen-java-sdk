package no.kommune.oslo.nokkelen.sdk.controller;

import no.kommune.oslo.nokkelen.sdk.AdapterController;
import no.kommune.oslo.nokkelen.sdk.MessageSink;
import no.kommune.oslo.nokkelen.api.messages.AdapterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractAdapterController implements AdapterController {

  private final static Logger log = LoggerFactory.getLogger(AbstractAdapterController.class);

  protected final AtomicReference<MessageSink> sink = new AtomicReference<>();

  @Override
  public void onConnection(MessageSink messageSink) {
    log.info("Got connection");
    sink.set(messageSink);
  }

  @Override
  public void onDisconnected() {
    log.info("Lost backend connection");
    sink.set(null);
  }

  public void sendMessage(AdapterMessage message) {
    MessageSink messageSink = sink.get();

    if (messageSink != null) {
      messageSink.sendMessage(message);
    }
    else {
      throw new IllegalStateException("No connection, can't send " + message);
    }
  }

}
