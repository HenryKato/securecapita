package io.hkfullstack.securecapita.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@SuperBuilder // used when you have an inheritance hierarchy of classes, and you want to generate builder classes for all classes in that hierarchy
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class User {
    private Long id;
    @NotEmpty(message = "First name cannot be empty")
    private String firstName;
    @NotEmpty(message = "Last name cannot be empty")
    private String lastName;
    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Please enter valid email")
    private String email;
    @NotEmpty(message = "Password cannot be empty")
    private String password;
    private String address;
    private String phone;
    private String title;
    private String bio;
    private boolean enabled;
    private boolean isNotLocked;
    private boolean isUsingMfa;
    private String createdAt;
    private String imageUrl;
}
