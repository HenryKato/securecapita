package io.hkfullstack.securecapita.controller;

import io.hkfullstack.securecapita.dto.UserDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

import static io.hkfullstack.securecapita.dtomapper.UserDTOMapper.*;
import static io.hkfullstack.securecapita.utils.ExceptionUtils.processError;
import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.security.authentication.UsernamePasswordAuthenticationToken.unauthenticated;

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
    private static final String TOKEN_PREFIX = "Bearer ";

    @PostMapping("/login")
    public  ResponseEntity<SecureApiResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
        UserDTO user = getAuthenticatedUser(authentication);
        return user.isUsingMfa() ? sendVerificationCode(user) : sendApiResponse(user);
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

    @PutMapping("/verify/account/{key}")
    public ResponseEntity<SecureApiResponse> verifyAccount(@PathVariable("key") String key) {
        UserDTO user = userService.verifyAccountKey(key);
        return ResponseEntity.ok()
                .body(SecureApiResponse.builder().timestamp(now().toString())
                        .message(user.isEnabled() ? "Account already verified" : "Your account is now verified")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/resetpassword/{email}")
    public ResponseEntity<SecureApiResponse> resetPassword(@PathVariable("email") String email) {
        userService.resetPassword(email);
        return ResponseEntity.created(getUri()).body(
                SecureApiResponse.builder().timestamp(now().toString())
                        .message("Password reset url sent. Please check your email.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
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

    @GetMapping("/refresh/token")
    public ResponseEntity<SecureApiResponse> generateNewAccessToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION).substring(TOKEN_PREFIX.length());
        if(isHeaderAndTokenValid(request, token)) {
            UserDTO user = userService.getUserByEmail(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok()
                    .body(SecureApiResponse.builder().timestamp(now().toString())
                            .payload(of("user", user,
                                    "access_token", tokenProvider.generateAccessToken(getUserPrincipal(user)),
                                    "refresh_token", token))
                            .message("New Access Token Sent").status(OK).statusCode(OK.value()).build());
        } else {
            return ResponseEntity.badRequest()
                    .body(SecureApiResponse.builder().timestamp(now().toString())
                            .reason("Refresh Token Invalid or Missing in your request")
                            .status(BAD_REQUEST).statusCode(BAD_REQUEST.value()).build());
        }
    }

    private boolean isHeaderAndTokenValid(HttpServletRequest request, String token) {
        String email = tokenProvider.getSubject(token, request);
        String requestAuthHeader = request.getHeader(AUTHORIZATION);
        return tokenProvider.isTokenValid(email, token) && requestAuthHeader != null && requestAuthHeader.startsWith(TOKEN_PREFIX);
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<SecureApiResponse> verifyPasswordUrl(@PathVariable("key") String key) {
        UserDTO user = userService.verifyPasswordKey(key);
        return ResponseEntity.ok()
                .body(SecureApiResponse.builder().timestamp(now().toString())
                        .payload(of("user", user))
                        .message("Please enter a new password.")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @PutMapping ("/reset/password/{key}/{password}/{confirmPassword}")
    public ResponseEntity<SecureApiResponse> updateUserPassword(@PathVariable("key") String key, @PathVariable("password") String password, @PathVariable("confirmPassword") String confirmPassword) {
        userService.updateUserPassword(key, password, confirmPassword);
        return ResponseEntity.ok()
                .body(SecureApiResponse.builder().timestamp(now().toString())
                        .message("Your password has been updated successfully...")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
    }

    @GetMapping("/profile")
    public ResponseEntity<SecureApiResponse> profile(Authentication authentication) {
        UserDTO user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok()
                .body(SecureApiResponse.builder()
                        .timestamp(now().toString())
                        .payload(of("user", user))
                        .message("Profile retrieved successfully...")
                        .status(OK)
                        .statusCode(OK.value())
                        .build());
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
        return new UserPrincipal(toUser(userService.getUserByEmail(user.getEmail())), roleService.getRoleByUserEmail(user));
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

    private UserDTO getAuthenticatedUser(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserDTO userDTO = principal.getUser();
        return userDTO;
    }

    private Authentication authenticateUser(String email, String password) {
        try {
            return authenticationManager.authenticate(unauthenticated(email, password));
        } catch (Exception ex) {
            processError(request, response, ex);
            throw new ApiException(ex.getMessage());
        }
    }

}
