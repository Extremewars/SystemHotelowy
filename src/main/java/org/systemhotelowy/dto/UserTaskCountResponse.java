package org.systemhotelowy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskCountResponse {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private Long taskCount;
}
