package com.web.agua_facil.services;

import java.util.List;
import com.web.agua_facil.models.User;

public interface UserService {
    List<User> getAllUsers();
    User saveUser(User user, String cpf);
    User getUserById(Long id);
    void deleteUserById(Long id);
    User findByEmail(String email);
    User updateUser(Long id, User userEditado);
}