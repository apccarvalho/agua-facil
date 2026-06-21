package com.web.agua_facil.controllers;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.agua_facil.models.PropertyCategory;
import com.web.agua_facil.models.TariffTier;
import com.web.agua_facil.services.TariffTierService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/tariff")
public class TariffTierController {

    private final TariffTierService tariffTierService;

    public TariffTierController(TariffTierService tariffTierService) {
        this.tariffTierService = tariffTierService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("tariffsList", tariffTierService.getAllTariffTiers());
        return "tariff/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("tariffTier", new TariffTier());
        model.addAttribute("categorias", PropertyCategory.values());
        return "tariff/create";
    }

    @PostMapping("/save")
    public String saveTariffTier(@Valid @ModelAttribute("tariffTier") TariffTier tariffTier,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "tariff/create";
        }

        try {
            tariffTierService.saveTariffTier(tariffTier);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Faixa de tarifa registrada com sucesso!");
            return "redirect:/tariff";
        } catch (IllegalArgumentException e) {

            model.addAttribute("erroTarifa", e.getMessage());
            return "tariff/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        
        Optional<TariffTier> tariffOpt = tariffTierService.findTariffById(id);
        
        if (tariffOpt.isPresent()) {
            model.addAttribute("tariffTier", tariffOpt.get());
            model.addAttribute("categorias", PropertyCategory.values());
            return "tariff/edit";
        } else {
            redirectAttributes.addFlashAttribute("erro", "Faixa de tarifa não encontrada.");
            return "redirect:/tariff";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateTariffTier(@PathVariable Long id, 
                                   @Valid @ModelAttribute("tariffTier") TariffTier tariffTierDetails,
                                   BindingResult result, Model model, 
                                   RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
        	model.addAttribute("categorias", PropertyCategory.values());
            return "tariff/edit";
        }

        try {
            tariffTierService.updateTariffTier(id, tariffTierDetails);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Faixa de tarifa atualizada com sucesso!");
            return "redirect:/tariff";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erroTarifa", e.getMessage());
            model.addAttribute("categorias", PropertyCategory.values());
            return "tariff/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTariffTier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tariffTierService.deleteTariffTier(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Faixa de tarifa excluída com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/tariff";
    }
}