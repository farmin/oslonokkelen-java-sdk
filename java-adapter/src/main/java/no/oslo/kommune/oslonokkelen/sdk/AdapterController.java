package no.oslo.kommune.oslonokkelen.sdk;


import no.oslo.kommune.oslonokkelen.adapter.messages.AckMessage;

import java.util.concurrent.ScheduledExecutorService;

public interface AdapterController extends AutoCloseable {

  /**
   *
   * @param state This should be kept up to date with attached things, action and health status
   * @param executor Can be used to schedule health checks and other regular tasks
   */
  void start(LocalState state, ScheduledExecutorService executor);

  /**
   * Will be invoked after a connection has been established.
   *
   * @param messageSink Can be used to send messages to our backend
   */
  void onConnection(MessageSink messageSink);

  /**
   * Will be invoked whenever we loose connection
   */
  void onDisconnected();


  void ack(AckMessage ackMessage);

}
