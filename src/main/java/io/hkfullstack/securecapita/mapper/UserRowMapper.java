package io.hkfullstack.securecapita.mapper;

import io.hkfullstack.securecapita.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException { // Add all the user fields
        return User.builder()
                .id(resultSet.getLong("id"))
                .firstName(resultSet.getString("first_name"))
                .lastName(resultSet.getString("last_name"))
                .password(resultSet.getString("password"))
                .title(resultSet.getString("title"))
                .address(resultSet.getString("address"))
                .phone(resultSet.getString("phone"))
                .bio(resultSet.getString("bio"))
                .enabled(resultSet.getBoolean("enabled"))
                .isNotLocked(resultSet.getBoolean("non_locked"))
                .isUsingMfa(resultSet.getBoolean("using_mfa"))
                .email(resultSet.getString("email"))
                .createdAt(resultSet.getString("created_at"))
                .imageUrl(resultSet.getString("image_url"))
                .build();
    }
}
