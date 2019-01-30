package no.kommune.oslo.nokkelen.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JacksonFactory {

  public static ObjectMapper createObjectMapper() {
    ObjectMapper jackson = new ObjectMapper();
    jackson.registerModule(new Jdk8Module());
    jackson.registerModule(new JavaTimeModule());

    return jackson;
  }

}
