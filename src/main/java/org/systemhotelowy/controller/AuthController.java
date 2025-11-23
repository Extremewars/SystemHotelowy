package org.systemhotelowy.controller;


import org.systemhotelowy.dto.LoginRequest;
import org.systemhotelowy.dto.UserRequest;
import org.systemhotelowy.dto.UserResponse;
import org.systemhotelowy.model.ROLE;
import org.systemhotelowy.model.User;
import org.systemhotelowy.service.JwtService;
import org.systemhotelowy.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Niepoprawne dane");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequest request){
        // Wymuś domyślną rolę USER, jeśli nie podano
        if (request.getRole() == null) {
            request.setRole(ROLE.USER);
        }
        // Prosta walidacja danych minimalnych
        if (request.getEmail() == null || request.getEmail().isBlank() ||
            request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Email i hasło są wymagane");
        }
        // Sprawdzenie konfliktu po email
        if (userService.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Użytkownik o podanym email już istnieje");
        }
        // Mapowanie DTO -> encja
        User u = new User();
        u.setFirstName(request.getFirstName());
        u.setLastName(request.getLastName());
        u.setEmail(request.getEmail());
        u.setPassword(request.getPassword()); // Zostanie zakodowane w serwisie
        u.setRole(request.getRole());
        u.setAddress(request.getAddress());

        User created = userService.create(u);

        UserResponse resp = new UserResponse();
        resp.setId(created.getId());
        resp.setFirstName(created.getFirstName());
        resp.setLastName(created.getLastName());
        resp.setEmail(created.getEmail());
        resp.setRole(created.getRole());
        resp.setAddress(created.getAddress());

        return ResponseEntity.created(URI.create("/api/users/" + created.getId())).body(resp);
    }
}