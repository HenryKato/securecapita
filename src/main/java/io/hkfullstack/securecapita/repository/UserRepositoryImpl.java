package io.hkfullstack.securecapita.repository;

import io.hkfullstack.securecapita.exception.ApiException;
import io.hkfullstack.securecapita.mapper.UserRowMapper;
import io.hkfullstack.securecapita.model.Role;
import io.hkfullstack.securecapita.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.hkfullstack.securecapita.enumeration.RoleType.ROLE_USER;
import static io.hkfullstack.securecapita.enumeration.VerificationType.ACCOUNT;
import static io.hkfullstack.securecapita.query.UserQuery.*;

@Repository
@RequiredArgsConstructor //Constructor with only fields marked with final or @NonNull
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder passwordEncoder; //Bean must be defined

    @Override
    @Transactional // If the role is not assigned to the user, then rollback everything
    public User createUser(User user) {
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0 ) throw new ApiException("Email already in use. Please get a different one!"); //  Check that the email is unique
        //  Save the User
        try {
            log.info("Creating User...");
            KeyHolder newUserHolder = new GeneratedKeyHolder(); //it's key contains the auto-generated id of the newly saved user
            SqlParameterSource userSqlParameters = getUserSqlParameters(user); // user fields on which the query is executed
            namedParameterJdbcTemplate.update(INSERT_USER_QUERY, userSqlParameters, newUserHolder); //save the new user
            user.setId(Objects.requireNonNull(newUserHolder.getKey()).longValue()); // newUserHolder.getKey() returns the new Id which is then set to the original user object

            log.info("Adding Role to User {}", user);
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name()); //  Add role to the user. We've already set the userId, so we can get it
            String accountVerificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType()); //  Generate account verification url
            namedParameterJdbcTemplate.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, //  Save account verification url
                    Map.of("userId", user.getId(), "url", accountVerificationUrl)); // Account verification table needs the userId and the verification url
            //  Send account verification url to the user via email
            // emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), accountVerificationUrl, ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);

            //Return the newly created user
            return user;
            //  If any errors, throw exception with proper message
        }
        catch (Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User getUser(Long id) {
        return null;
    }

    @Override
    public Collection<User> getUsers(int page, int pageSize) {
        return null;
    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public Boolean deleteUser(Long id) {
        return null;
    }

    @Override
    public User findUserByUsername(String username) {
        try {
            Map<String, String> queryNamedParametersMap = Map.of("username", username);
            User user =namedParameterJdbcTemplate.queryForObject(FIND_USER_BY_EMAIL_QUERY, queryNamedParametersMap, new UserRowMapper());
            return user;
        } catch(EmptyResultDataAccessException ex) {
            log.error("User with email {} is not found", username);
            throw new ApiException("User not found with email: " + username);
        } catch (Exception ex) {
            log.error("error: {} ", ex.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }

    }

    private Integer getEmailCount(String email) {
        Map<String, String> queryNamedParametersMap = Map.of("email", email); // parameters or fields on which the sql query is executed
        return namedParameterJdbcTemplate.queryForObject(COUNT_USER_EMAIL_QUERY, queryNamedParametersMap, Integer.class);
    }
    private SqlParameterSource getUserSqlParameters(User user) { // parameters used to save a user into the database
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", passwordEncoder.encode(user.getPassword())); // we can't store a raw password inside the database
    }

    private String getVerificationUrl(String urlKey, String verificationType) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/verify/" + verificationType + "/" + urlKey).toUriString(); // Backend Url which we will change in the UI since context path of the UI is different
    }
}
