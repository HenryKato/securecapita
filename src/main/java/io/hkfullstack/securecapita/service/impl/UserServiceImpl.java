package io.hkfullstack.securecapita.service.impl;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.dtomapper.UserDTOMapper;
import io.hkfullstack.securecapita.exception.ApiException;
import io.hkfullstack.securecapita.model.Role;
import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.repository.RoleRepository;
import io.hkfullstack.securecapita.repository.UserRepository;
import io.hkfullstack.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Date;
import java.util.UUID;

import static io.hkfullstack.securecapita.dtomapper.UserDTOMapper.fromUser;
import static io.hkfullstack.securecapita.enumeration.VerificationType.ACCOUNT;
import static io.hkfullstack.securecapita.enumeration.VerificationType.PASSWORD;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.time.DateFormatUtils.format;
import static org.apache.commons.lang3.time.DateUtils.addDays;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;
    private static final String DATE_PATTERN = "yyyy-MM-dd hh:mm:ss";
    @Override
    public UserDTO createUser(User user) {
        String accountVerificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType()); //  Generate account verification url
        return mapToUserDTO(userRepository.createUser(user, accountVerificationUrl));
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        return mapToUserDTO(userRepository.findUserByUsername(email));
    }

    @Override
    public void sendVerificationCode(UserDTO user) {
        String verificationCode = randomAlphanumeric(6).toUpperCase();
        String expirationDate = format(addDays(new Date(), 1), DATE_PATTERN);
        userRepository.sendVerificationCode(user, verificationCode, expirationDate);
    }

    @Override
    public UserDTO verifyCode(String email, String code) {
        return mapToUserDTO(userRepository.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        String passwordResetUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
        String urlExpirationDate = format(addDays(new Date(), 1), DATE_PATTERN);
        userRepository.resetPassword(email, passwordResetUrl, urlExpirationDate);
    }

    @Override
    public UserDTO verifyPasswordKey(String key) {
        String passwordUrl = getVerificationUrl(key, PASSWORD.getType());
        return mapToUserDTO(userRepository.verifyPasswordKey(passwordUrl));
    }

    @Override
    public void updateUserPassword(String key, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new ApiException("Passwords don not match.");
        String passwordVerificationUrl = getVerificationUrl(key, PASSWORD.getType());
        userRepository.updateUserPassword(password, passwordVerificationUrl);
    }

    @Override
    public UserDTO verifyAccountKey(String key) {
        String accountVerificationUrl = getVerificationUrl(key, ACCOUNT.getType());
        return mapToUserDTO(userRepository.verifyAccountKey(accountVerificationUrl));
    }

    private String getVerificationUrl(String urlKey, String verificationType) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/users/verify/" + verificationType + "/" + urlKey).toUriString(); // Backend Url which we will change in the UI since context path of the UI is different
    }

    private UserDTO mapToUserDTO(User user) {
        return fromUser(user, roleRepository.getRoleByUserEmail(user.getEmail()));
    }
}
