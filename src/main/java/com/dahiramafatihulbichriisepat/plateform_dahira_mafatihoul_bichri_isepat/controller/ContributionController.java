package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Contribution;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.ContributionService;

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
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContributionController {

    private final ContributionService contributionService;
    private final UserRepository userRepository;

    /**
     * Récupérer toutes les contributions.
     * À réserver aux administrateurs autorisés.
     */
    @GetMapping("/contributions")
    public List<Contribution> getAllContributions() {
        return contributionService.getAllContributions();
    }

    /**
     * Récupérer les contributions du membre connecté.
     */
    @GetMapping("/my-contributions")
    public List<Contribution> getMyContributions(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Utilisateur non authentifié");
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable"));

        if (user.getMemberId() == null) {
            return List.of();
        }

        return contributionService.getContributionsByMemberId(
                user.getMemberId());
    }

    /**
     * Récupérer une contribution par son identifiant.
     */
    @GetMapping("/contributions/{id}")
    public Contribution getContributionById(@PathVariable Long id) {
        return contributionService.getContributionById(id);
    }

    /**
     * Créer une contribution.
     */
    @PostMapping("/contributions")
    @ResponseStatus(HttpStatus.CREATED)
    public Contribution createContribution(
            @Valid @RequestBody Contribution contribution) {
        return contributionService.createContribution(contribution);
    }

    /**
     * Modifier une contribution.
     */
    @PutMapping("/contributions/{id}")
    public Contribution updateContribution(
            @PathVariable Long id,
            @Valid @RequestBody Contribution contributionDetails) {
        return contributionService.updateContribution(id, contributionDetails);
    }

    /**
     * Supprimer une contribution.
     */
    @DeleteMapping("/contributions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContribution(@PathVariable Long id) {
        contributionService.deleteContribution(id);
    }

}
