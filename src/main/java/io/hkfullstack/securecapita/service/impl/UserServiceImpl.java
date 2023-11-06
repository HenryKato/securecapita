package io.hkfullstack.securecapita.service.impl;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.dtomapper.UserDTOMapper;
import io.hkfullstack.securecapita.model.Role;
import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.repository.RoleRepository;
import io.hkfullstack.securecapita.repository.UserRepository;
import io.hkfullstack.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.hkfullstack.securecapita.dtomapper.UserDTOMapper.fromUser;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;
    @Override
    public UserDTO createUser(User user) {
        return mapToUserDTO(userRepository.createUser(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.findUserByUsername(email));
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        userRepository.sendVerificationCode(user);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email, code));
    }

    private UserDTO mapToUserDTO(User user) {
        return fromUser(user, roleRepository.getRoleByUserEmail(user.getEmail()));
    }
}
