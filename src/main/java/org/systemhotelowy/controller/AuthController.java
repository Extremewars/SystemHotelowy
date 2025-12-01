package org.systemhotelowy.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.systemhotelowy.dto.LoginRequest;
import org.systemhotelowy.dto.LoginResponse;
import org.systemhotelowy.dto.UserRequest;
import org.systemhotelowy.dto.UserResponse;
import org.systemhotelowy.mapper.UserMapper;
import org.systemhotelowy.model.Role;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.JwtService;
import org.systemhotelowy.service.UserService;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Niepoprawne dane");
        }
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(jwt, "Bearer"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRequest request){
        if (request.getRole() == null) {
            request.setRole(Role.USER);
        }
        
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Użytkownik o podanym email już istnieje");
        }

        User user = userMapper.toEntity(request);
        User created = userService.create(user);
        UserResponse response = userMapper.toResponse(created);

        return ResponseEntity.created(URI.create("/api/users/" + created.getId()))
                .body(response);
    }
}