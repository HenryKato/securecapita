package io.hkfullstack.securecapita.service.impl;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.Role;
import io.hkfullstack.securecapita.repository.RoleRepository;
import io.hkfullstack.securecapita.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository<Role> roleRepository;
    @Override
    public Role getRoleByUserEmail(UserDTO user) {
        return roleRepository.getRoleByUserEmail(user.getEmail());
    }
}
