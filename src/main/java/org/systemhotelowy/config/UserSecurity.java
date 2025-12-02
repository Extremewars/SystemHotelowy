package org.systemhotelowy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.systemhotelowy.service.UserService;

@Component("userSecurity")
public class UserSecurity {
    @Autowired
    private UserService userService;

    public boolean isOwner(Authentication authentication, Integer userId) {
        String username = authentication.getName();
        return userService.findById(userId)
                .map(user -> user.getEmail().equals(username))
                .orElse(false);
    }
}