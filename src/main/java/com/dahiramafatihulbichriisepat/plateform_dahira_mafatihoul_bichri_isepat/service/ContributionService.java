package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Contribution;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.ContributionRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionRepository contributionRepository;

    /**
     * Récupérer toutes les contributions.
     */
    public List<Contribution> getAllContributions() {
        return contributionRepository.findAll();
    }

    /**
     * Récupérer les contributions d'un membre.
     */
    public List<Contribution> getContributionsByMemberId(Long memberId) {
        return contributionRepository.findByMemberId(memberId);
    }

    /**
     * Récupérer une contribution par son identifiant.
     */
    public Contribution getContributionById(Long id) {
        return contributionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Contribution introuvable avec l'id : " + id));
    }

    /**
     * Créer une contribution.
     */
    @Transactional
    public Contribution createContribution(Contribution contribution) {
        return contributionRepository.save(contribution);
    }

    /**
     * Modifier une contribution.
     */
    @Transactional
    public Contribution updateContribution(
            Long id,
            Contribution contributionDetails) {

        Contribution contribution = getContributionById(id);

        contribution.setMemberId(contributionDetails.getMemberId());
        contribution.setEventOrProjectName(
                contributionDetails.getEventOrProjectName());
        contribution.setRequestedAmount(
                contributionDetails.getRequestedAmount());
        contribution.setPaidAmount(
                contributionDetails.getPaidAmount());
        contribution.setContributionDate(
                contributionDetails.getContributionDate());
        contribution.setStatus(contributionDetails.getStatus());
        contribution.setPaymentMethod(
                contributionDetails.getPaymentMethod());
        contribution.setObservation(
                contributionDetails.getObservation());

        return contributionRepository.save(contribution);
    }

    /**
     * Supprimer une contribution.
     */
    @Transactional
    public void deleteContribution(Long id) {
        contributionRepository.delete(getContributionById(id));
    }

}
