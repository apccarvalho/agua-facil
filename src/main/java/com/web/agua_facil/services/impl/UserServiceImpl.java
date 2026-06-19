package com.web.agua_facil.services.impl;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.web.agua_facil.models.User;
import com.web.agua_facil.models.UserRole;
import com.web.agua_facil.repositories.ClientRepository;
import com.web.agua_facil.repositories.UserRepository;
import com.web.agua_facil.services.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           ClientRepository clientRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User saveUser(User user, String cpf) {
        
        if (user.getRole() == UserRole.CLIENTE) {
            if (cpf == null || cpf.trim().isEmpty()) {
                throw new IllegalArgumentException("O CPF é obrigatório para o perfil CLIENTE.");
            }
            String cpfLimpo = cpf.replaceAll("\\D", ""); 
            user.setPassword(passwordEncoder.encode(cpfLimpo));
        } 
        else {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("A senha é obrigatória para este perfil.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }
    
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com id: " + id));
    }
    
    @Transactional
    public User updateUser(Long id, User userEditado) {

        User usuarioExistente = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com id: " + id));

        usuarioExistente.setNome(userEditado.getNome());
        usuarioExistente.setEmail(userEditado.getEmail());
        usuarioExistente.setRole(userEditado.getRole());

        String novaSenha = userEditado.getPassword();
        if (novaSenha != null && !novaSenha.trim().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(novaSenha));
        }

        return userRepository.save(usuarioExistente);
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