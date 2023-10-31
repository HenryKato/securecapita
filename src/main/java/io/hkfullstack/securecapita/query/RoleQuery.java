package io.hkfullstack.securecapita.query;

public class RoleQuery {
    public static final String SELECT_ROLE_BY_NAME_QUERY = "SELECT * FROM Roles WHERE name = :roleName";
    public static final String INSERT_ROLE_TO_USER_QUERY = "INSERT INTO UserRoles (user_id, role_id) VALUES (:userId, :roleId)";
    public static final String FIND_ROLE_ID_BY_USER_ID = "SELECT * FROM UserRoles WHERE user_id = :userId";
    public static final String FIND_ROLE_BY_USERNAME = "SELECT r.* \n" +
            "FROM Roles r \n" +
            "WHERE r.id = (\n" +
            "    SELECT ur.role_id \n" +
            "    FROM UserRoles ur \n" +
            "    WHERE ur.user_id = (\n" +
            "        SELECT u.id \n" +
            "        FROM Users u \n" +
            "        WHERE u.email = :username\n" +
            "    )\n" +
            ");\n";
}
