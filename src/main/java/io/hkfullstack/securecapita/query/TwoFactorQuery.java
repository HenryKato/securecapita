package io.hkfullstack.securecapita.query;

public class TwoFactorQuery {
  public static final String DELETE_EXISTING_CODE_BY_USER_ID_QUERY = "DELETE FROM TwoFactorVerifications WHERE user_id = :userId";
  public static final String INSERT_NEW_CODE_BY_USER_ID_QUERY = "INSERT INTO TwoFactorVerifications (user_id, code, code_exp_date) VALUES (:userId, :code, :expirationDate)";
}
