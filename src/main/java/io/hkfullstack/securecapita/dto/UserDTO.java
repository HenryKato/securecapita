package io.hkfullstack.securecapita.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;
    private String title;
    private String bio;
    private boolean enabled;
    private boolean isNotLocked;
    private boolean isUsingMfa;
    private String createdAt;
    private String imageUrl;
    private String roleName;
    private String permissions;
}
