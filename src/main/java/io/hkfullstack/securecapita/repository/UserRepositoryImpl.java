package io.hkfullstack.securecapita.repository;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.exception.ApiException;
import io.hkfullstack.securecapita.mapper.UserRowMapper;
import io.hkfullstack.securecapita.model.Role;
import io.hkfullstack.securecapita.model.UpdateUserRequest;
import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.utils.TwilioUtils;
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

import jakarta.transaction.Transactional;

import java.util.*;

import static io.hkfullstack.securecapita.enumeration.RoleType.ROLE_USER;
import static io.hkfullstack.securecapita.query.TwoFactorQuery.DELETE_EXISTING_CODE_BY_USER_ID_QUERY;
import static io.hkfullstack.securecapita.query.TwoFactorQuery.INSERT_NEW_CODE_BY_USER_ID_QUERY;
import static io.hkfullstack.securecapita.query.UserQuery.*;
import static java.util.Map.of;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.time.DateFormatUtils.format;

@Repository
@RequiredArgsConstructor //Constructor with only fields marked with final or @NonNull
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder passwordEncoder; //Bean must be defined

    @Override
    @Transactional // If the role is not assigned to the user, then rollback everything
    public User createUser(User user, String accountVerificationUrl) {
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
            namedParameterJdbcTemplate.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, //  Save account verification url
                    of("userId", user.getId(), "url", accountVerificationUrl)); // Account verification table needs the userId and the verification url
            //  Send account verification url to the user via email
            // emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), accountVerificationUrl, ACCOUNT);
            user.setEnabled(true);
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
        try {
            return namedParameterJdbcTemplate.queryForObject(FIND_USER_BY_ID_QUERY, of("id", id), new UserRowMapper());
        } catch(EmptyResultDataAccessException ex) {
            log.error("User with id {} is not found", id);
            throw new ApiException("User not found with id: " + id);
        } catch (Exception ex) {
            log.error("error: {} ", ex.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public Collection<User> getUsers(int page, int pageSize) {
        return null;
    }

    @Override
    public User updateUser(UpdateUserRequest request) {
        try {
            SqlParameterSource userDetailsSqlParameters = getUserDetailsSqlParameters(request);
            namedParameterJdbcTemplate.update(UPDATE_USER_DETAILS, userDetailsSqlParameters);
            return getUser(request.getId());
        } catch(EmptyResultDataAccessException ex) {
            log.error("User with email {} is not found", request.getEmail());
            throw new ApiException("User not found with email: " + request.getEmail());
        }
    }

    @Override
    public Boolean deleteUser(Long id) {
        return null;
    }

    @Override
    public User findUserByUsername(String username) {
        try {
            Map<String, String> queryNamedParametersMap = of("username", username);
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

    @Override
    public void sendVerificationCode(UserDTO user, String verificationCode, String expirationDate) {
        // Generate the code - Leverage Apache Commons API
        try {
            // Delete any existing code(s) for this user in the TwoFactorVerifications table
            namedParameterJdbcTemplate.update(DELETE_EXISTING_CODE_BY_USER_ID_QUERY, of("userId", user.getId()));
            // Save the new code into the TwoFactorVerifications Table
            namedParameterJdbcTemplate.update(INSERT_NEW_CODE_BY_USER_ID_QUERY, of("userId", user.getId(), "code", verificationCode, "expirationDate", expirationDate));
            // Send the code in an SMS text message - Leverage Twilio but for I'll do something else
            // sendSMS(user.getPhone(), "Your Secure Capita Code: \n " + verificationCode);
            log.info("Verification Code: {} ", verificationCode);
        } catch (Exception ex) {
            log.error("error: {} ", ex.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        User userByEmail = findUserByUsername(email);
        try {
            User userByCode = namedParameterJdbcTemplate.queryForObject(FIND_USER_BY_CODE_QUERY, of("code", code, "expDate", new Date()), new UserRowMapper());
                if(userByCode.getEmail().equalsIgnoreCase(userByEmail.getEmail())) {
                    namedParameterJdbcTemplate.update(DELETE_EXISTING_CODE_BY_USER_ID_QUERY, of("userId", userByCode.getId()));
                    return userByCode;
                } else {
                    throw new ApiException("User with email: " + email + ", does not exist. Please login again.");
                }
        } catch(EmptyResultDataAccessException ex) {
            log.error("Invalid code {} ", code);
            throw new ApiException("Invalid code: " + code + ", Please login again");
        } catch (Exception ex) {
            log.error("error: {} ", ex.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    @Transactional
    public void resetPassword(String email, String passwordResetUrl, String urlExpirationDate) {
        if(getEmailCount(email.trim().toLowerCase()) <= 0) throw new ApiException("User with email " + email + "doesn't exist");
        try {
                User user = findUserByUsername(email);
                namedParameterJdbcTemplate.update(DELETE_EXISTING_PASSWORD_RESET_URL_BY_USER_ID_QUERY, of("userId", user.getId()));
                namedParameterJdbcTemplate.update(INSERT_NEW_PASSWORD_RESET_URL_BY_USER_ID_QUERY, of("userId", user.getId(), "url", passwordResetUrl, "expirationDate", urlExpirationDate));
                log.info("Reset password url: {} ", passwordResetUrl);
                // TODO: send email with password reset url, to the user
        } catch (Exception ex) {
            log.error("error: {} ", ex.getMessage());
            throw new ApiException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyPasswordKey(String passwordUrl) {
        if(isUrlExpired(passwordUrl)) throw new ApiException("This link is expired. Please reset your password again.");
        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY, of("url", passwordUrl), new UserRowMapper());
        } catch(EmptyResultDataAccessException ex) {
            log.error(ex.getMessage());
            throw new ApiException("This link is invalid. Please reset your password again.");
        } catch(Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public void updateUserPassword(String password, String passwordVerificationUrl) {
        try {
            namedParameterJdbcTemplate.update(UPDATE_USER_PASSWORD_BY_VERIFICATION_URL_QUERY, of("url", passwordVerificationUrl, "newPassword", passwordEncoder.encode(password)));
            namedParameterJdbcTemplate.update(DELETE_PASSWORD_VERIFICATION_URL_BY_URL_QUERY, of("url", passwordVerificationUrl));
        } catch (Exception ex) {
            throw new ApiException("An error occurred. Please try again");
        }
    }

    @Override
    public User verifyAccountKey(String accountVerificationUrl) {
        try {
            User user = namedParameterJdbcTemplate.queryForObject(SELECT_USER_BY_ACCOUNT_VERIFICATION_URL_QUERY, of("url", accountVerificationUrl), new UserRowMapper());
            namedParameterJdbcTemplate.update(UPDATE_USER_ENABLED_QUERY, of("enabled", true, "userId", user.getId()));
            return user;
        } catch(EmptyResultDataAccessException ex) {
            throw new ApiException("This link is invalid. Please verify your account again.");
        } catch(Exception ex) {
            throw new ApiException("An error occurred. Please try again");
        }
    }

    private Boolean isUrlExpired(String passwordUrl) {
        try {
            return namedParameterJdbcTemplate.queryForObject(SELECT_EXPIRATION_BY_PASSWORD_URL_QUERY, of("url", passwordUrl), Boolean.class);
        } catch(EmptyResultDataAccessException ex) {
            log.error(ex.getMessage());
            throw new ApiException("This link is invalid. Please reset your password again.");
        } catch(Exception ex) {
            log.error(ex.getMessage());
            throw new ApiException("An error occurred. Please try again");
        }
    }

    private void sendSMS(String phone, String verificationCode) {
        TwilioUtils.sendSMS(phone, verificationCode);
    }

    private Integer getEmailCount(String email) {
        Map<String, String> queryNamedParametersMap = of("email", email); // parameters or fields on which the sql query is executed
        return namedParameterJdbcTemplate.queryForObject(COUNT_USER_EMAIL_QUERY, queryNamedParametersMap, Integer.class);
    }
    private SqlParameterSource getUserSqlParameters(User user) { // parameters used to save a user into the database
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", passwordEncoder.encode(user.getPassword())); // we can't store a raw password inside the database
    }

    private SqlParameterSource getUserDetailsSqlParameters(UpdateUserRequest request) { // parameters used to save a user into the database
        return new MapSqlParameterSource()
                .addValue("id", request.getId())
                .addValue("firstName", request.getFirstName())
                .addValue("lastName", request.getLastName())
                .addValue("email", request.getEmail())
                .addValue("phone", request.getPhone())
                .addValue("address", request.getAddress())
                .addValue("bio", request.getBio())
                .addValue("title", request.getTitle());
    }
}
