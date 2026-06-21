package com.web.agua_facil.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.agua_facil.models.Property;
import com.web.agua_facil.models.Reading;
import com.web.agua_facil.services.PropertyService;
import com.web.agua_facil.services.ReadingService;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/reading")
public class ReadingController {

    private final ReadingService readingService;
    private final PropertyService propertyService;

    public ReadingController(ReadingService readingService, PropertyService propertyService) {
        this.readingService = readingService;
        this.propertyService = propertyService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("readingsList", readingService.getAllReadings());
        return "reading/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("reading", new Reading());
        carregarListasFormulario(model);
        return "reading/create";
    }

    @PostMapping("/save")
    public String saveReading(@Valid @ModelAttribute("reading") Reading reading,
                              BindingResult result, Model model,
                              RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            carregarListasFormulario(model);
            return "reading/create";
        }

        try {
            readingService.registerReading(reading);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Leitura registrada com sucesso!");
            return "redirect:/reading";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erroLeitura", e.getMessage());
            carregarListasFormulario(model);
            return "reading/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Reading reading = readingService.findReadingById(id);
            model.addAttribute("reading", reading);
            carregarListasFormulario(model);
            return "reading/edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/reading";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateReading(@PathVariable Long id, @Valid @ModelAttribute("reading") Reading reading,
                                BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            carregarListasFormulario(model);
            return "reading/edit";
        }

        try {
            readingService.updateReading(id, reading);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Leitura atualizada com sucesso!");
            return "redirect:/reading";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erroLeitura", e.getMessage());
            carregarListasFormulario(model);
            return "reading/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteReading(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            readingService.deleteReading(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Leitura excluída com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/reading";
    }

    private void carregarListasFormulario(Model model) {
        List<Property> properties = propertyService.getAllProperties();
        model.addAttribute("properties", properties);
    }
}