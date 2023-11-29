package io.hkfullstack.securecapita.utils;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.UserPrincipal;
import org.springframework.security.core.Authentication;

public class UserUtils {
    public static UserDTO getAuthenticatedUser(Authentication authentication) {
        return ((UserDTO) authentication.getPrincipal());
    }

    public static UserDTO getLoggedInUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return principal.getUser();
    }
}
