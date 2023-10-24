package io.hkfullstack.securecapita.controller;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.User;
import io.hkfullstack.securecapita.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/")
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid User user) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("response-header", "test-resp-header");
        return new ResponseEntity<>(userService.createUser(user), headers, HttpStatus.CREATED);
    }

}
