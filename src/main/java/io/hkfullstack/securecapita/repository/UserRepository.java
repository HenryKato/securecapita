package io.hkfullstack.securecapita.repository;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.User;

import java.util.Collection;

public interface UserRepository<T extends User> {
    /* Basic CRUD Operations */
    T createUser(T user);
    T getUser(Long id);
    Collection<T> getUsers(int page, int pageSize);
    void updateUser(T user);
    Boolean deleteUser(Long id);

    /*Complex CRUD Operations*/
    T findUserByUsername(String username);
    void sendVerificationCode(UserDTO user);

    User verifyCode(String email, String code);
}
