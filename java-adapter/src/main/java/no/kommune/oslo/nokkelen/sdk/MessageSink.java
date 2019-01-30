package no.kommune.oslo.nokkelen.sdk;


import no.kommune.oslo.nokkelen.api.messages.AdapterMessage;

public interface MessageSink {

  /**
   * Send a message back to our backend!
   * @param message To be sent
   */
  void sendMessage(AdapterMessage message);

}
