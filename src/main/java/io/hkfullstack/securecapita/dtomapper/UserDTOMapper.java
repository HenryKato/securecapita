package io.hkfullstack.securecapita.dtomapper;

import io.hkfullstack.securecapita.dto.UserDTO;
import io.hkfullstack.securecapita.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserDTOMapper {
    public static UserDTO fromUser(User user) {
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }

    public static User toUser(UserDTO userDTO) {
        User user = User.builder().build();
        BeanUtils.copyProperties(userDTO, user);
        return  user;
    }
}
