package org.example.service;

import org.example.entity.User;

public interface UserService {
    void createUser(User user);
    User getUserById(Long id);
}
