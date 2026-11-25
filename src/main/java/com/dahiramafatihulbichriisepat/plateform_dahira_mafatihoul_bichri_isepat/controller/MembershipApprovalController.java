package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.PendingMemberResponse;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.MembershipApprovalService;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/membership-requests")
@RequiredArgsConstructor

// Les trois rôles peuvent accéder au contrôleur.
@PreAuthorize("""
            hasAnyRole(
                'ADMINISTRATEUR',
                'TRESORIER',
                'ADMIN_PRINCIPAL'
            )
        """)
public class MembershipApprovalController {

    private final MembershipApprovalService approvalService;

    /**
     * Consultation des demandes d'adhésion.
     *
     * ADMINISTRATEUR : autorisé
     * TRESORIER : autorisé
     * ADMIN_PRINCIPAL : autorisé
     */
    @GetMapping
    public List<PendingMemberResponse> getPendingRequests() {

        return approvalService.getPendingRequests();
    }

    /**
     * Acceptation d'une demande.
     *
     * TRESORIER : autorisé
     * ADMIN_PRINCIPAL : autorisé
     * ADMINISTRATEUR : interdit
     */
    @PutMapping("/{userId}/approve")
    @PreAuthorize("hasAnyRole('TRESORIER', 'ADMIN_PRINCIPAL')")
    public PendingMemberResponse approve(
            @PathVariable Long userId) {

        return approvalService.approve(userId);
    }

    /**
     * Refus d'une demande.
     *
     * TRESORIER : autorisé
     * ADMIN_PRINCIPAL : autorisé
     * ADMINISTRATEUR : interdit
     */
    @PutMapping("/{userId}/reject")
    @PreAuthorize("hasAnyRole('TRESORIER', 'ADMIN_PRINCIPAL')")
    public PendingMemberResponse reject(
            @PathVariable Long userId) {

        return approvalService.reject(userId);
    }
}