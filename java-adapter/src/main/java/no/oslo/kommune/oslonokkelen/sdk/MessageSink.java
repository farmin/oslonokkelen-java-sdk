package no.oslo.kommune.oslonokkelen.sdk;


import no.oslo.kommune.oslonokkelen.adapter.messages.AdapterMessage;

public interface MessageSink {

  /**
   * Send a message back to our backend!
   * @param message To be sent
   */
  void sendMessage(AdapterMessage message);

}
