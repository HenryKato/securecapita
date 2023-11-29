package io.hkfullstack.securecapita.service;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.UpdateUserRequest;
import io.hkfullstack.securecapita.model.User;

public interface UserService {
    UserDTO createUser(User user);
    UserDTO getUserByEmail(String email);
    void sendVerificationCode(UserDTO user);
    UserDTO verifyCode(String email, String code);
    void resetPassword(String email);
    UserDTO verifyPasswordKey(String key);
    void updateUserPassword(String key, String password, String confirmPassword);

    UserDTO verifyAccountKey(String key);

    UserDTO updateUser(UpdateUserRequest request);

    UserDTO getUserById(Long userId);
}
