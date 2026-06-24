package com.web.agua_facil.controllers;

import com.web.agua_facil.models.Service;
import com.web.agua_facil.services.ServiceService;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/service")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public String listServices(Model model) {
        model.addAttribute("servicesList", serviceService.getAllServices());
        return "service/index";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("service", new Service());
        return "service/create";
    }

    @PostMapping("/create")
    public String saveService(@Valid @ModelAttribute("service") Service service,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "service/create";
        }
        
        serviceService.saveService(service);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Serviço cadastrado com sucesso!");
        return "redirect:/service";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Service> serviceOpt = serviceService.findServiceById(id);
        
        if (serviceOpt.isPresent()) {
            model.addAttribute("service", serviceOpt.get());
            return "service/edit";
        } else {
            redirectAttributes.addFlashAttribute("erro", "Serviço não encontrado.");
            return "redirect:/service";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateService(@PathVariable Long id,
                                @Valid @ModelAttribute("service") Service serviceDetails,
                                BindingResult result,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        
        if (result.hasErrors()) {
            return "service/edit";
        }
        
        try {
            serviceService.updateService(id, serviceDetails);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Serviço atualizado com sucesso!");
            return "redirect:/service";
        } catch (RuntimeException e) {
            model.addAttribute("erro", e.getMessage());
            return "service/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            serviceService.deleteService(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Serviço excluído com sucesso!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/service";
    }
}