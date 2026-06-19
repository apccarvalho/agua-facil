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
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("usersList", userService.getAllUsers());
        return "user/index";
    }

    @GetMapping("/novo")
    public String create(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", UserRole.values());
        return "user/create";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("user") User user, BindingResult result,
                       @RequestParam(required = false) String cpf,
                       Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", UserRole.values());
            return "user/create";
        }
        userService.saveUser(user, cpf);
        return "redirect:/user";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUserById(id));
        model.addAttribute("roles", UserRole.values());
        return "user/edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") Long id, @Valid @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "user/edit";
        }
     
        userService.updateUser(id, user);
        
        return "redirect:/user"; 
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/user";
    }
}