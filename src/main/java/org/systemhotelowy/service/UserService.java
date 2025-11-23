package org.systemhotelowy.service;

import org.systemhotelowy.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User create(User user);
    Optional<User> findById(Integer id);
    List<User> findAll();
    Optional<User> findByEmail(String email);
    User update(User user);
    void deleteById(Integer id);
}