package com.web.agua_facil.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.web.agua_facil.models.User;
import com.web.agua_facil.models.UserRole;
import com.web.agua_facil.services.UserService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/funcionario/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("usersList", userService.getAllUsers());
        return "funcionario/usuarios/index";
    }

    @GetMapping("/novo")
    public String create(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        return "funcionario/usuarios/create";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("user") User user, BindingResult result,
                       @RequestParam(required = false) String cpf,
                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "funcionario/usuarios/create";
        }
        userService.saveUser(user, cpf);
        return "redirect:/funcionario/usuarios";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", UserRole.values());
        return "funcionario/usuarios/edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("user") User user,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "funcionario/usuarios/edit";
        }
        user.setId(id);
        userService.saveUser(user, null);
        return "redirect:/funcionario/usuarios";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/funcionario/usuarios";
    }
}