package no.oslo.kommune.oslonokkelen.sdk.health;

import no.oslo.kommune.oslonokkelen.sdk.MessageSink;

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
