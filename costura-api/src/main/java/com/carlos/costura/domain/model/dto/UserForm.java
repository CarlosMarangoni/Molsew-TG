package com.carlos.costura.domain.model.dto;

import com.carlos.costura.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserForm {

    private String username;

    private String name;

    private String email;

    private String password;

    public static UserForm toForm(User user){
        return new UserForm(
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPassword()
        );
    }
}
