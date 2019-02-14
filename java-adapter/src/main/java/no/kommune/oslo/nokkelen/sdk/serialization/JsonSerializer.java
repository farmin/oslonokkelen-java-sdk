package no.kommune.oslo.nokkelen.sdk.serialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonSerializer {

  private final static ObjectMapper jackson = new ObjectMapper();

  static {
    jackson.registerModule(new Jdk8Module());
    jackson.registerModule(new JavaTimeModule());
  }


  public static <T> T fromJson(String json, Class<T> type) {
    try {
      return jackson.readValue(json, type);
    }
    catch (Exception ex) {
      String error = String.format("Failed to create %s from: %s", type, json);
      throw new IllegalStateException(error, ex);
    }
  }

  public static String toJson(Object obj) {
    try {
      return jackson.writeValueAsString(obj);
    }
    catch (Exception ex) {
      String error = String.format("Failed to turn %s into json", obj);
      throw new IllegalStateException(error, ex);
    }
  }

  public static JsonNode readTree(String json) {
    try {
      return jackson.readTree(json);
    }
    catch (Exception ex) {
      String error = String.format("Failed to extract message type from: %s", json);
      throw new IllegalStateException(error, ex);
    }
  }

}
