package com.web.agua_facil.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.web.agua_facil.models.Bill;
import com.web.agua_facil.models.BillStatus;
import com.web.agua_facil.services.BillService;
import com.web.agua_facil.services.ServiceService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/bill")
public class BillController {

    private final BillService billService;
    private final ServiceService serviceService;

    public BillController(BillService billService, ServiceService serviceService) {
        this.billService = billService;
        this.serviceService = serviceService;
    }

    @GetMapping
    public String listBills(Model model) {
        model.addAttribute("billsList", billService.getAllBills());
        return "bill/index";
    }

    @GetMapping("/generate/{readingId}")
    public String showPreGenerateForm(@PathVariable Long readingId, Model model) {
        model.addAttribute("readingId", readingId);
        model.addAttribute("servicesList", serviceService.getAllServices()); 
        
        return "bill/pre-generate";
    }
    
    @PostMapping("/generate/{readingId}")
    public String generateBill(
            @PathVariable Long readingId, 
            @RequestParam(value = "servicosIds", required = false) List<Long> servicosIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            billService.gerarFatura(readingId, servicosIds);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Fatura gerada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        
        return "redirect:/bill"; 
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Bill> billOpt = billService.findBillById(id);
        
        if (billOpt.isPresent()) {
            model.addAttribute("bill", billOpt.get());
            model.addAttribute("statuses", BillStatus.values()); 
            return "bill/edit";
        } else {
            redirectAttributes.addFlashAttribute("erro", "Fatura não encontrada.");
            return "redirect:/bill";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBill(@PathVariable Long id, 
                             @Valid @ModelAttribute("bill") Bill billDetails,
                             BindingResult result, Model model, 
                             RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("statuses", BillStatus.values());
            return "bill/edit";
        }

        try {
            billService.updateBill(id, billDetails);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Fatura atualizada com sucesso!");
            return "redirect:/bill";
        } catch (IllegalArgumentException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("statuses", BillStatus.values());
            return "bill/edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteBill(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            billService.deleteBill(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Fatura excluída com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/bill";
    }
}