package io.hkfullstack.securecapita.model;

import io.hkfullstack.securecapita.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

import static io.hkfullstack.securecapita.dtomapper.UserDTOMapper.fromUser;
import static java.util.Arrays.stream;

@RequiredArgsConstructor
public class UserPrincipal implements UserDetails  {

    private final User user;
    private final Role role;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return stream(role.getPermission().split(",".trim())).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.user.isNotLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.user.isEnabled();
    }

    public UserDTO getUser() {
        return fromUser(this.user, role);
    }
}
