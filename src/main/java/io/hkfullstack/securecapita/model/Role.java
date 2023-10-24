package io.hkfullstack.securecapita.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder // used when you have an inheritance hierarchy of classes, and you want to generate builder classes for all classes in that hierarchy
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Role {
    private Long id;
    private String name;
    private String permission;
}
