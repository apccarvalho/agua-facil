package com.web.agua_facil.services.impl;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.User;
import com.web.agua_facil.models.UserRole;
import com.web.agua_facil.repositories.ClientRepository;
import com.web.agua_facil.repositories.UserRepository;
import com.web.agua_facil.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           ClientRepository clientRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User saveUser(User user, String cpf) {
        if (user.getRole() == UserRole.CLIENTE) {
            if (cpf == null || cpf.isBlank()) {
                throw new IllegalArgumentException("CPF é obrigatório para clientes");
            }

            user.setPassword(passwordEncoder.encode(cpf));
            User savedUser = userRepository.save(user);

            Client client = new Client();
            client.setUser(savedUser);
            client.setCpf(cpf);
            clientRepository.save(client);

            return savedUser;
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com id: " + id));
    }

    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuário não encontrado com id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com email: " + email));
    }
}