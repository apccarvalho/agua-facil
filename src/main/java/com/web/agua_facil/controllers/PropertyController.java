package com.web.agua_facil.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.agua_facil.models.Client;
import com.web.agua_facil.models.Property;
import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.services.ClientService;
import com.web.agua_facil.services.PropertyService;
import jakarta.validation.Valid;
import java.util.List;


@Controller
@RequestMapping("/property")
public class PropertyController {

    private final PropertyService propertyService;
    private final ClientService clientService;

    public PropertyController(PropertyService propertyService, ClientService clientService) {
        this.propertyService = propertyService;
        this.clientService = clientService;
    }

    @GetMapping
    public String listProperties(Model model) {
        model.addAttribute("propertiesList", propertyService.getAllProperties());
        return "property/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("property", new Property());
        carregarListasFormulario(model);
        return "property/create";
    }

    @PostMapping("/save")
    public String saveProperty(@Valid @ModelAttribute("property") Property property,
                               BindingResult result, Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            carregarListasFormulario(model);
            return "property/create";
        }

        try {
            propertyService.saveProperty(property);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Imóvel registado com sucesso!");
            return "redirect:/property";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erroMatricula", e.getMessage());
            carregarListasFormulario(model);
            return "property/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Property property = propertyService.findPropertyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Imóvel inválido: " + id));
        
        model.addAttribute("property", property);
        carregarListasFormulario(model);
        return "property/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateProperty(@PathVariable Long id, @Valid @ModelAttribute("property") Property property,
                                 BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            carregarListasFormulario(model);
            return "property/edit";
        }

        try {
            propertyService.updateProperty(id, property);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Imóvel atualizado com sucesso!");
            return "redirect:/property";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erroMatricula", e.getMessage());
            carregarListasFormulario(model);
            return "property/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProperty(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        propertyService.deleteProperty(id);
        return "redirect:/property";
    }

    private void carregarListasFormulario(Model model) {
        List<Client> clientes = clientService.getAllClients();     
        model.addAttribute("clientes", clientes);
        model.addAttribute("categorias", PropertyCategory.values());
    }
}