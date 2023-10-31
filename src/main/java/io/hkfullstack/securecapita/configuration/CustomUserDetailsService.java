package io.hkfullstack.securecapita.configuration;


import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.model.UserPrincipal;
import io.hkfullstack.securecapita.repository.RoleRepository;
import io.hkfullstack.securecapita.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username);
        String permissions = roleRepository.getRoleByUserEmail(username).getPermission();
        return new UserPrincipal(user, permissions);
    }
}
