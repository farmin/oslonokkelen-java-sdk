package no.kommune.oslo.nokkelen.sdk.auth;

public interface AuthClient extends AutoCloseable {

  /**
   * Authenticate using client credentials.
   *
   * @param credentials Client credentials
   * @return Refresh and access tokens
   */
  AuthToken authenticate(ClientCredentials credentials);

  /**
   * Use the refresh token to get a new access token.
   *
   * According to rfc6749 section-6 we have to supply
   * the client credentials when refreshing tokens for
   * a confidential client even though we have a valid
   * refresh token handy.
   *
   * @param credentials Client credentials.
   * @param oldToken The old token
   * @return A new token
   */
  AuthToken refresh(ClientCredentials credentials, AuthToken oldToken);

  /**
   * It is a good idea to log out the session
   * when we know we won't be using the refresh
   * token again.
   *
   * @param credentials Client credentials (see explanation in refresh)
   * @param token To invalidate
   */
  void logout(ClientCredentials credentials, AuthToken token);

}
