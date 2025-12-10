package org.systemhotelowy.mapper;

import org.springframework.stereotype.Component;
import org.systemhotelowy.dto.UserRequest;
import org.systemhotelowy.dto.UserResponse;
import org.systemhotelowy.model.User;

@Component
public class UserMapper {

    public User toEntity(UserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UserRequest nie może być nullem");
        }
        
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        
        return user;
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            throw new IllegalArgumentException("UserResponse nie może być nullem");
        }
        
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        
        return response;
    }
}
