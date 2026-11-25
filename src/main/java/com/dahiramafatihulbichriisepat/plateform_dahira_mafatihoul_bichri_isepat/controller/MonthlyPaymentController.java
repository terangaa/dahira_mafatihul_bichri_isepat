package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.GenerateMonthlyPaymentsRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.GenerateMonthlyPaymentsResponse;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.MonthlyPayment;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.MonthlyPaymentService;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MonthlyPaymentController {

    private final MonthlyPaymentService monthlyPaymentService;

    private final UserRepository userRepository;

    // =========================================================
    // MES COTISATIONS
    // =========================================================

    @GetMapping("/my-monthly-payments")
    public List<MonthlyPayment> getMyMonthlyPayments(
            Principal principal) {

        User user = userRepository
                .findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Utilisateur introuvable."));

        if (user.getMemberId() == null) {
            return List.of();
        }

        return monthlyPaymentService
                .getMonthlyPaymentsByMemberId(
                        user.getMemberId());
    }

    // =========================================================
    // TOUTES LES COTISATIONS
    // =========================================================

    @GetMapping("/monthly-payments")
    public List<MonthlyPayment> getAllMonthlyPayments() {

        return monthlyPaymentService
                .getAllMonthlyPayments();
    }

    // =========================================================
    // UNE COTISATION
    // =========================================================

    @GetMapping("/monthly-payments/{id}")
    public MonthlyPayment getMonthlyPaymentById(
            @PathVariable Long id) {

        return monthlyPaymentService
                .getMonthlyPaymentById(id);
    }

    // =========================================================
    // COTISATIONS D'UN MEMBRE
    // =========================================================

    @GetMapping("/monthly-payments/member/{memberId}")
    public List<MonthlyPayment> getMonthlyPaymentsByMember(
            @PathVariable Long memberId) {

        return monthlyPaymentService
                .getMonthlyPaymentsByMemberId(memberId);
    }

    // =========================================================
    // COTISATIONS D'UNE PÉRIODE
    // =========================================================

    @GetMapping("/monthly-payments/period/{month}/{year}")
    public List<MonthlyPayment> getMonthlyPaymentsByPeriod(
            @PathVariable Integer month,
            @PathVariable Integer year) {

        return monthlyPaymentService
                .getMonthlyPaymentsByPeriod(
                        month,
                        year);
    }

    // =========================================================
    // CRÉER
    // =========================================================

    @PostMapping("/monthly-payments")
    @ResponseStatus(HttpStatus.CREATED)
    public MonthlyPayment createMonthlyPayment(
            @Valid @RequestBody MonthlyPayment monthlyPayment) {

        return monthlyPaymentService
                .createMonthlyPayment(
                        monthlyPayment);
    }

    // =========================================================
    // GÉNÉRER
    // =========================================================

    @PostMapping("/monthly-payments/generate")
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateMonthlyPaymentsResponse generateMonthlyPayments(
            @Valid @RequestBody GenerateMonthlyPaymentsRequest request) {

        return monthlyPaymentService
                .generateMonthlyPayments(request);
    }

    // =========================================================
    // MODIFIER
    // =========================================================

    @PutMapping("/monthly-payments/{id}")
    public MonthlyPayment updateMonthlyPayment(
            @PathVariable Long id,
            @Valid @RequestBody MonthlyPayment monthlyPaymentDetails) {

        return monthlyPaymentService
                .updateMonthlyPayment(
                        id,
                        monthlyPaymentDetails);
    }

    // =========================================================
    // ANNULER
    // =========================================================

    @PostMapping("/monthly-payments/{id}/cancel")
    public MonthlyPayment cancelMonthlyPayment(
            @PathVariable Long id) {

        return monthlyPaymentService
                .cancelMonthlyPayment(id);
    }

    // =========================================================
    // SUPPRESSION INTERDITE
    // =========================================================

    @DeleteMapping("/monthly-payments/{id}")
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public void deleteMonthlyPayment(
            @PathVariable Long id) {

        throw new IllegalStateException(
                "La suppression définitive d'une cotisation "
                        + "est interdite. Utilisez l'annulation "
                        + "afin de conserver l'historique financier.");
    }
}