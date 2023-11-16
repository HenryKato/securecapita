package io.hkfullstack.securecapita.repository;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.User;

import java.util.Collection;

public interface UserRepository<T extends User> {
    /* Basic CRUD Operations */
    T createUser(T user, String accountVerificationUrl);
    T getUser(Long id);
    Collection<T> getUsers(int page, int pageSize);
    void updateUser(T user);
    Boolean deleteUser(Long id);

    /*Complex CRUD Operations*/
    T findUserByUsername(String username);
    void sendVerificationCode(UserDTO user, String verificationCode, String expirationDate);

    User verifyCode(String email, String code);

    void resetPassword(String email, String resetPasswordUrl, String expirationDate);

    T verifyPasswordKey(String passwordUrl);

    void updateUserPassword(String password, String passwordVerificationUrl);
}
