package io.hkfullstack.securecapita.repository;

import io.hkfullstack.securecapita.exception.ApiException;
import io.hkfullstack.securecapita.mapper.RoleRowMapper;
import io.hkfullstack.securecapita.model.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.hkfullstack.securecapita.enumeration.RoleType.ROLE_USER;
import static io.hkfullstack.securecapita.enumeration.VerificationType.ACCOUNT;
import static io.hkfullstack.securecapita.query.RoleQuery.INSERT_ROLE_TO_USER_QUERY;
import static io.hkfullstack.securecapita.query.RoleQuery.SELECT_ROLE_BY_NAME_QUERY;
import static io.hkfullstack.securecapita.query.UserQuery.INSERT_ACCOUNT_VERIFICATION_URL_QUERY;
import static io.hkfullstack.securecapita.query.UserQuery.INSERT_USER_QUERY;

@Repository
@RequiredArgsConstructor //Constructor with only fields marked with final or @NonNull
@Slf4j
public class RoleRepositoryImpl implements  RoleRepository<Role>{

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    @Override
    public Role createRole(Role role) {
        return null;
    }

    @Override
    public Role getRole(Long id) {
        return null;
    }

    @Override
    public Collection<Role> getRoles(int page, int pageSize) {
        return null;
    }

    @Override
    public void updateRole(Role user) {

    }

    @Override
    public Boolean deleteRole(Long id) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        try {
            // Get the role by name from the Roles table
            Role role = namedParameterJdbcTemplate.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName", roleName), new RoleRowMapper());
            // Insert the role to the user in the UserRoles table
            namedParameterJdbcTemplate.update(INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", Objects.requireNonNull(role).getId()));
        }
        /*
         * EmptyResultDataAccessException is typically thrown when a query or operation on a database
         * using Spring's Data Access Object (DAO) framework returns no results when you expect at least one result
         * This exception typically occurs when using methods like queryForObject() or queryForList() in Spring's JDBC template or similar operations when working with databases.
         */
        catch (EmptyResultDataAccessException ex) {
            throw new ApiException("No role found by name: " + ROLE_USER.name());
        } catch (Exception ex) {
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        return null;
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
