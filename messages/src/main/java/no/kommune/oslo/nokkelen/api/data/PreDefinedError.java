package no.kommune.oslo.nokkelen.api.data;

public enum PreDefinedError {

  /* Permanent errors
   */


  /**
   * Typically an error we didn't expect / handled.
   */
  UNKNOWN_ERROR("unknown-error"),


  /* Temporary error
   */



  ;


  private final String code;

  PreDefinedError(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

}
