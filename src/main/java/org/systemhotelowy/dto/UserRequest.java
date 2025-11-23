package org.systemhotelowy.dto;

import org.systemhotelowy.model.ROLE;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private ROLE role;
    private String address;
}