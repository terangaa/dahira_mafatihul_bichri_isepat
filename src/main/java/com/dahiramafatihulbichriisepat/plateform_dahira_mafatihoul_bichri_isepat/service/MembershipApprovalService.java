
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.PendingMemberResponse;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AccountStatus;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Member;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MemberRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MembershipApprovalService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final EmailNotificationService emailNotificationService;

    public MembershipApprovalService(
            UserRepository userRepository,
            MemberRepository memberRepository,
            EmailNotificationService emailNotificationService) {

        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.emailNotificationService = emailNotificationService;
    }

    // ============================================================
    // LISTE DES DEMANDES EN ATTENTE
    // ============================================================

    @Transactional(readOnly = true)
    public List<PendingMemberResponse> getPendingRequests() {

        return userRepository.findAll()
                .stream()
                .filter(user -> user.getAccountStatus() == AccountStatus.PENDING)
                .filter(user -> user.getMemberId() != null)
                .map(this::toResponse)
                .toList();
    }

    // ============================================================
    // APPROBATION
    // ============================================================

    @Transactional
    public PendingMemberResponse approve(Long userId) {

        User user = findPendingUser(userId);
        Member member = findAssociatedMember(user);

        // Modifier les statuts.
        user.setAccountStatus(AccountStatus.APPROVED);
        member.setStatus(Member.MemberStatus.ACTIVE);

        userRepository.save(user);
        memberRepository.save(member);

        // Préparer la réponse avant la notification.
        PendingMemberResponse response = toResponse(user);

        // Notifier le demandeur.
        // EmailNotificationService intercepte les erreurs d'envoi.
        emailNotificationService.notifyMembershipApproved(
                member.getFirstName() + " " + member.getLastName(),
                user.getEmail());

        return response;
    }

    // ============================================================
    // REJET
    // ============================================================

    @Transactional
    public PendingMemberResponse reject(Long userId) {

        User user = findPendingUser(userId);
        Member member = findAssociatedMember(user);

        // Modifier les statuts.
        user.setAccountStatus(AccountStatus.REJECTED);
        member.setStatus(Member.MemberStatus.INACTIVE);

        userRepository.save(user);
        memberRepository.save(member);

        // Préparer la réponse avant la notification.
        PendingMemberResponse response = toResponse(user);

        // Aucun motif n'est encore demandé par le contrôleur actuel.
        emailNotificationService.notifyMembershipRejected(
                member.getFirstName() + " " + member.getLastName(),
                user.getEmail(),
                null);

        return response;
    }

    // ============================================================
    // RECHERCHE D'UN COMPTE EN ATTENTE
    // ============================================================

    private User findPendingUser(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Compte utilisateur introuvable."));

        if (user.getAccountStatus() != AccountStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cette demande a déjà été traitée.");
        }

        return user;
    }

    // ============================================================
    // RECHERCHE DE LA FICHE MEMBRE
    // ============================================================

    private Member findAssociatedMember(User user) {

        if (user.getMemberId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Aucune fiche membre n'est associée à ce compte.");
        }

        return memberRepository.findById(user.getMemberId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La fiche membre associée à ce compte est introuvable."));
    }

    // ============================================================
    // CONVERSION EN DTO
    // ============================================================

    private PendingMemberResponse toResponse(User user) {

        Member member = findAssociatedMember(user);

        return new PendingMemberResponse(
                member.getId(),
                user.getId(),
                member.getFirstName(),
                member.getLastName(),
                user.getEmail(),
                member.getPhone(),
                member.getCommission(),
                member.getFiliere(),
                member.getPromotion(),
                member.getRegistrationDate());
    }
}
