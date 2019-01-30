package no.kommune.oslo.nokkelen.sdk.health;

import no.kommune.oslo.nokkelen.sdk.MessageSink;

import java.util.concurrent.atomic.AtomicReference;

public abstract class MessageTask implements Runnable {

  protected final AtomicReference<MessageSink> sink;

  public MessageTask(AtomicReference<MessageSink> sink) {
    this.sink = sink;
  }

  protected boolean isDisconnected() {
    return sink.get() == null;
  }

}
