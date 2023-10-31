package io.hkfullstack.securecapita.controller;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.ApiResponse;
import io.hkfullstack.securecapita.model.LoginRequest;
import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;

import static java.time.LocalDateTime.now;
import static java.util.Map.of;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public  ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        UserDTO userDTO = userService.getUserByEmail(request.getEmail());
        return ResponseEntity.ok()
                .body(
                        ApiResponse.builder()
                                .timestamp(now().toString())
                                .payload(of("user", userDTO))
                                .message("Logged in successfully...")
                                .status(OK)
                                .statusCode(OK.value())
                                .build()
                );
    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse> createUser(@RequestBody @Valid User user) {
        UserDTO userDto = userService.createUser(user);
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("response-header", "test-resp-header");
        return ResponseEntity.created(getUri()).body(
                ApiResponse.builder()
                        .timestamp(now().toString())
                        .payload(of("user", userDto))
                        .message("User created")
                        .status(CREATED)
                        .statusCode(CREATED.value())
                        .build()
        );
    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/get/<userId>").toUriString());
    }

}
