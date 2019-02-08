package no.kommune.oslo.nokkelen.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocalStateTest {


  @Test
  @DisplayName("Create thing")
  void create_thing() {
    LocalState state = new LocalState();

    state.addThing("laptop", (tb) -> {
      tb.readableProperties("uptime", "temperature");

      tb.action("lock", (ob) -> {
        ob.requiredUserParameters("id");
        return (request) -> ExecResult.success("Yey");
      });

      tb.action("unlock", (ob) -> {
        ob.requiredActionParameters("id");
        return (request) -> ExecResult.error("x", "F...");
      });
    });
  }


}