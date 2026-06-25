package com.web.agua_facil.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.web.agua_facil.models.Bill;
import com.web.agua_facil.models.Client;
import com.web.agua_facil.services.BillService;
import com.web.agua_facil.services.ClientService;

@Controller
@RequestMapping("/portal")
@PreAuthorize("hasRole('CLIENTE')")
public class PortalController {

    private final ClientService clientService;
    private final BillService billService;

    public PortalController(ClientService clientService, BillService billService) {
        this.clientService = clientService;
        this.billService = billService;
    }

    @GetMapping("/minha-conta")
    public String minhaConta(Principal principal, Model model) {

        String emailLogado = principal.getName();
        
        Client client = clientService.findByUserEmail(emailLogado)
            .orElseThrow(() -> new RuntimeException("Perfil de cliente não encontrado para o usuário logado."));
            
        List<Bill> ultimasFaturas = billService.getTop5FaturasRecentesDoCliente(client.getId());
        
        model.addAttribute("client", client);
        model.addAttribute("ultimasFaturas", ultimasFaturas);
        
        return "client/perfil";
    }
}