package org.example.dao;

import org.example.entity.User;

public interface UserDao {
    void save(User user);
    User findById(Long id);
}
