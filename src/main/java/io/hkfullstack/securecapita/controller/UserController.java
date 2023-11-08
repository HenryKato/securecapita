package io.hkfullstack.securecapita.controller;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.dtomapper.UserDTOMapper;
import io.hkfullstack.securecapita.exception.ApiException;
import io.hkfullstack.securecapita.model.SecureApiResponse;
import io.hkfullstack.securecapita.model.LoginRequest;
import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.model.UserPrincipal;
import io.hkfullstack.securecapita.provider.TokenProvider;
import io.hkfullstack.securecapita.service.RoleService;
import io.hkfullstack.securecapita.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

import static io.hkfullstack.securecapita.utils.ExceptionUtils.processError;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @PostMapping("/login")
    public  ResponseEntity<SecureApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        UserDTO user = getAuthenticatedUser(authentication);
        System.out.println(user.toString());
        System.out.println(authentication);
        return user.isUsingMfa() ? sendVerificationCode(user) : sendApiResponse(user);
    }

    private UserDTO getAuthenticatedUser(Authentication authentication) {
        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }

    private Authentication authenticateUser(String email, String password) {
        try {
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception ex) {
            processError(request, response, ex);
            throw new ApiException(ex.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<SecureApiResponse> createUser(@RequestBody @Valid User user) {
        UserDTO userDto = userService.createUser(user);
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("response-header", "test-resp-header");
        return ResponseEntity.created(getUri()).body(
                SecureApiResponse.builder().timestamp(now().toString())
                        .payload(of("user", userDto)).message("User created")
                        .status(CREATED).statusCode(CREATED.value()).build());
    }

    @GetMapping("/verify/{email}/{code}")
    public ResponseEntity<SecureApiResponse> verifyCode(@PathVariable("email") String email, @PathVariable("code") String code) {
        UserDTO user = userService.verifyCode(email, code);
        return ResponseEntity.ok()
                .body(SecureApiResponse.builder().timestamp(now().toString())
                        .payload(of("user", user,
                        "access_token", tokenProvider.generateAccessToken(getUserPrincipal(user)),
                        "refresh_token", tokenProvider.generateRefreshToken(getUserPrincipal(user))))
                        .message("Logged in successfully...").status(OK).statusCode(OK.value()).build());
    }

    @GetMapping("/profile")
    public ResponseEntity<SecureApiResponse> profile(Authentication authentication) {
        UserDTO user = getAuthenticatedUser(authentication);
        return ResponseEntity.ok()
                .body(SecureApiResponse.builder().timestamp(now().toString())
                        .payload(of("user", user)).message("Profile retrieved successfully...")
                        .status(OK).statusCode(OK.value()).build());
    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }

    private ResponseEntity<SecureApiResponse> sendApiResponse(UserDTO user) {
        return ResponseEntity.ok()
                .body(SecureApiResponse.builder().timestamp(now().toString()).payload(of(
                                        "user", user,
                                        "access_token", tokenProvider.generateAccessToken(getUserPrincipal(user)),
                                        "refresh_token", tokenProvider.generateRefreshToken(getUserPrincipal(user))
                                )).message("Logged in successfully...").status(OK).statusCode(OK.value()).build());
    }

    private UserPrincipal getUserPrincipal(UserDTO user) {
        return new UserPrincipal(UserDTOMapper.toUser(user), roleService.getRoleByUserEmail(user));
    }

    private ResponseEntity<SecureApiResponse> sendVerificationCode(UserDTO user) {
        userService.sendVerificationCode(user);
        return ResponseEntity.ok()
                .body( SecureApiResponse.builder()
                                .timestamp(now().toString()).payload(of("user", user))
                                .message("Verification Code sent").status(OK)
                                .statusCode(OK.value()).build()
                );
    }

}
