package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Member;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MemberRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    // ==========================================
    // CONSULTER TOUS LES MEMBRES
    // ==========================================

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // ==========================================
    // CONSULTER UN MEMBRE
    // ==========================================

    public Member getMemberById(Long id) {

        return memberRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Membre introuvable avec l'identifiant : " + id));
    }

    // ==========================================
    // AJOUTER UN MEMBRE
    // ==========================================

    @Transactional
    public Member createMember(Member member) {

        if (member.getStatus() == null) {
            member.setStatus(Member.MemberStatus.ACTIVE);
        }

        if (member.getRegistrationDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La date d'inscription est obligatoire.");
        }

        return memberRepository.save(member);
    }

    // ==========================================
    // MODIFIER UN MEMBRE
    // ==========================================

    @Transactional
    public Member updateMember(Long id, Member memberDetails) {

        Member member = getMemberById(id);

        member.setFirstName(memberDetails.getFirstName());
        member.setLastName(memberDetails.getLastName());
        member.setPhone(memberDetails.getPhone());
        member.setFiliere(memberDetails.getFiliere());
        member.setPromotion(memberDetails.getPromotion());
        member.setCommission(memberDetails.getCommission());

        if (memberDetails.getRegistrationDate() != null) {
            member.setRegistrationDate(memberDetails.getRegistrationDate());
        }

        if (memberDetails.getStatus() != null) {
            member.setStatus(memberDetails.getStatus());
        }

        return memberRepository.save(member);
    }

    // ==========================================
    // SUPPRIMER (DÉSACTIVER) UN MEMBRE
    // ==========================================

    @Transactional
    public void deleteMember(Long id) {

        Member member = getMemberById(id);

        // Suppression logique : conservation de l'historique.
        member.setStatus(Member.MemberStatus.INACTIVE);

        memberRepository.save(member);
    }
}