package io.hkfullstack.securecapita.service.impl;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.dtomapper.UserDTOMapper;
import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.repository.UserRepository;
import io.hkfullstack.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;
    @Override
    public UserDTO createUser(User user) {
        return UserDTOMapper.fromUser(userRepository.createUser(user));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return UserDTOMapper.fromUser(userRepository.findUserByUsername(email));
    }
}
