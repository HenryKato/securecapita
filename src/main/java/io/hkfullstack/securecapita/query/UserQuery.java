package io.hkfullstack.securecapita.query;

public class UserQuery {
    public static final String COUNT_USER_EMAIL_QUERY = "SELECT COUNT(*) FROM Users WHERE email = :email";
    public static final String INSERT_USER_QUERY = "INSERT INTO Users (first_name, last_name, email, password) VALUES (:firstName, :lastName, :email, :password)";
    public static final String INSERT_ACCOUNT_VERIFICATION_URL_QUERY = "INSERT INTO AccountVerifications (user_id, url) VALUES (:userId, :url)";
    public static final String FIND_USER_BY_EMAIL_QUERY = "SELECT * FROM Users WHERE email = :username";
    public static final String FIND_USER_BY_CODE_QUERY = "SELECT * FROM Users WHERE id = (SELECT user_id FROM TwoFactorVerifications WHERE code = :code AND code_exp_date >= :expDate)";
    public static final String DELETE_EXISTING_PASSWORD_RESET_URL_BY_USER_ID_QUERY = "DELETE FROM ResetPasswordVerifications WHERE user_id = :userId";
    public static final String INSERT_NEW_PASSWORD_RESET_URL_BY_USER_ID_QUERY = "INSERT INTO ResetPasswordVerifications (user_id, url, url_exp_date) VALUES (:userId, :url, :expirationDate)";
    public static final String SELECT_EXPIRATION_BY_PASSWORD_URL_QUERY = "SELECT url_exp_date < NOW() AS is_expired FROM ResetPasswordVerifications WHERE url = :url";
    public static final String SELECT_USER_BY_PASSWORD_URL_QUERY = "SELECT * FROM Users WHERE id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :url)";
    public static final String UPDATE_USER_PASSWORD_BY_VERIFICATION_URL_QUERY = "UPDATE Users SET password = :newPassword WHERE id = (SELECT user_id FROM ResetPasswordVerifications WHERE url = :url)";
    public static final String DELETE_PASSWORD_VERIFICATION_URL_BY_URL_QUERY = "DELETE FROM ResetPasswordVerifications WHERE url = :url";
    public static final String SELECT_USER_BY_ACCOUNT_VERIFICATION_URL_QUERY = "SELECT * FROM Users WHERE id = (SELECT user_id FROM AccountVerifications WHERE url = :url)";
    public static final String UPDATE_USER_ENABLED_QUERY = "UPDATE Users SET enabled = :enabled WHERE id = :userId";
    public static final String  UPDATE_USER_DETAILS = "UPDATE Users SET first_name = :firstName, last_name = :lastName, email = :email, phone = :phone, address = :address, title = :title, bio = :bio WHERE id = :id";
    public static final String FIND_USER_BY_ID_QUERY = "SELECT * FROM Users WHERE id = :id";
}