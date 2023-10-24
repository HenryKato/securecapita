package io.hkfullstack.securecapita.service;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.User;

public interface UserService {
    UserDTO createUser(User user);
}
