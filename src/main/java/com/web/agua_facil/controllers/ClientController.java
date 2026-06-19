package com.web.agua_facil.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.User;
import com.web.agua_facil.services.ClientService;
import jakarta.validation.Valid;

@Controller
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/client")
    public String index(Model model) {
        model.addAttribute("clientsList", clientService.getAllClients());
        return "client/index";
    }

    @GetMapping("/client/create")
    public String create(Model model) {
        Client client = new Client();
        client.setUser(new User());
        model.addAttribute("client", client);
        return "client/create";
    }

    @PostMapping("/client/save")
    public String save(@Valid @ModelAttribute("client") Client client, BindingResult result) {
        if (result.hasErrors()) {
            return "client/create";
        }
        clientService.saveClient(client);
        return "redirect:/client";
    }

    @GetMapping("/client/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.getClientById(id));
        return "client/edit";
    }

    @PostMapping("/client/edit/{id}")
    public String edit(@PathVariable Long id, @Valid @ModelAttribute("client") Client client, 
                       BindingResult result, RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "client/edit";
        }
        
        clientService.updateClient(id, client);
        
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cliente atualizado com sucesso!");
        
        return "redirect:/client";
    }

    @PostMapping("/client/delete/{id}")
    public String delete(@PathVariable Long id) {
        clientService.deleteClientById(id);
        return "redirect:/client";
    }
}