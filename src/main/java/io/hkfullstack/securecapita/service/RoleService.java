package io.hkfullstack.securecapita.service;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.Role;

public interface RoleService {
    Role getRoleByUserEmail(UserDTO user);
}
