
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.AdminAccountCreateRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.AdminAccountResponse;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.AdminAccountUpdateRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.AdminMemberRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AdminRole;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Member;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Role;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.MemberService;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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
public class MemberController {

        private final MemberService memberService;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        // =========================================================
        // MÉTHODES UTILITAIRES
        // =========================================================

        private String normalizeEmail(String email) {
                if (email == null || email.isBlank()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "L'adresse e-mail est obligatoire.");
                }

                return email.trim().toLowerCase(Locale.ROOT);
        }

        private User findUserByMemberId(Long memberId) {
                return userRepository.findByMemberId(memberId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Aucun compte lié à ce membre."));
        }

        private void validateRoles(Role role, AdminRole adminRole) {
                if (role == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Le rôle principal est obligatoire.");
                }

                if (role == Role.ADMIN && adminRole == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Un sous-rôle administratif est obligatoire.");
                }

                if (role == Role.MEMBER && adminRole != null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Un membre ne peut pas posséder de sous-rôle administratif.");
                }
        }

        private AdminAccountResponse toAccountResponse(User user) {
                return new AdminAccountResponse(
                                user.getId(),
                                user.getEmail(),
                                user.getRole(),
                                user.getAdminRole());
        }

        // =========================================================
        // PROFIL DU MEMBRE CONNECTÉ
        // =========================================================

        @GetMapping("/my-profile")
        public Member getMyProfile(Principal principal) {

                if (principal == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Vous devez vous connecter.");
                }

                User user = userRepository.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Utilisateur introuvable."));

                if (user.getMemberId() == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Aucune fiche membre n'est associée à ce compte.");
                }

                return memberService.getMemberById(user.getMemberId());
        }

        // =========================================================
        // LISTE ET CONSULTATION DES MEMBRES
        // =========================================================

        @GetMapping("/members")
        public List<Member> getAllMembers() {
                return memberService.getAllMembers();
        }

        @GetMapping("/members/{id}")
        public Member getMemberById(@PathVariable Long id) {
                return memberService.getMemberById(id);
        }

        @PostMapping("/members")
        @ResponseStatus(HttpStatus.CREATED)
        public Member createMember(@Valid @RequestBody Member member) {
                return memberService.createMember(member);
        }

        // =========================================================
        // CRÉER UN MEMBRE AVEC SON COMPTE
        // =========================================================

        @PostMapping("/admin/members-with-account")
        @ResponseStatus(HttpStatus.CREATED)
        @Transactional
        public Member createMemberWithAccount(
                        @Valid @RequestBody AdminMemberRequest request) {

                String email = normalizeEmail(request.getAccountEmail());

                if (userRepository.existsByEmail(email)) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Cette adresse e-mail est déjà utilisée.");
                }

                Role role = request.getAccountRole() == null
                                ? Role.MEMBER
                                : request.getAccountRole();

                /*
                 * Cette route ne reçoit actuellement aucun sous-rôle administratif.
                 * On refuse donc la création d'un ADMIN ici plutôt que de créer
                 * un compte administrateur incomplet.
                 */
                validateRoles(role, null);

                Member member = memberService.createMember(
                                Member.builder()
                                                .firstName(request.getFirstName())
                                                .lastName(request.getLastName())
                                                .phone(request.getPhone())
                                                .commission(request.getCommission())
                                                .filiere(request.getFiliere())
                                                .promotion(request.getPromotion())
                                                .registrationDate(request.getRegistrationDate())
                                                .status(request.getStatus())
                                                .build());

                User user = User.builder()
                                .email(email)
                                .password(passwordEncoder.encode(request.getAccountPassword()))
                                .role(role)
                                .adminRole(null)
                                .memberId(member.getId())
                                .build();

                userRepository.save(user);

                return member;
        }

        // =========================================================
        // CONSULTER LE COMPTE D'UN MEMBRE
        // =========================================================

        @GetMapping("/admin/members/{id}/account")
        public AdminAccountResponse getMemberAccount(
                        @PathVariable Long id) {

                User user = findUserByMemberId(id);

                return toAccountResponse(user);
        }

        // =========================================================
        // CRÉER UN COMPTE POUR UN MEMBRE EXISTANT
        // =========================================================

        @PostMapping("/admin/members/{id}/account")
        @ResponseStatus(HttpStatus.CREATED)
        @Transactional
        public AdminAccountResponse createMemberAccount(
                        @PathVariable Long id,
                        @Valid @RequestBody AdminAccountCreateRequest request) {

                // Vérifier que le membre existe.
                memberService.getMemberById(id);

                // Vérifier qu'il ne possède pas déjà un compte.
                if (userRepository.findByMemberId(id).isPresent()) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Ce membre possède déjà un compte.");
                }

                String email = normalizeEmail(request.getEmail());

                if (userRepository.existsByEmail(email)) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Cette adresse e-mail est déjà utilisée.");
                }

                validateRoles(request.getRole(), request.getAdminRole());

                if (request.getPassword() == null
                                || request.getPassword().length() < 8) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Le mot de passe doit contenir au moins 8 caractères.");
                }

                User user = User.builder()
                                .email(email)
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(request.getRole())
                                .adminRole(request.getRole() == Role.ADMIN
                                                ? request.getAdminRole()
                                                : null)
                                .memberId(id)
                                .build();

                User savedUser = userRepository.save(user);

                return toAccountResponse(savedUser);
        }

        // =========================================================
        // MODIFIER LE COMPTE D'UN MEMBRE
        // =========================================================

        @PutMapping("/admin/members/{id}/account")
        @Transactional
        public AdminAccountResponse updateMemberAccount(
                        @PathVariable Long id,
                        @Valid @RequestBody AdminAccountUpdateRequest request) {

                User user = findUserByMemberId(id);

                String email = normalizeEmail(request.getEmail());

                userRepository.findByEmail(email).ifPresent(existing -> {
                        if (!existing.getId().equals(user.getId())) {
                                throw new ResponseStatusException(
                                                HttpStatus.CONFLICT,
                                                "Cette adresse e-mail est déjà utilisée.");
                        }
                });

                validateRoles(request.getRole(), request.getAdminRole());

                user.setEmail(email);
                user.setRole(request.getRole());

                user.setAdminRole(
                                request.getRole() == Role.ADMIN
                                                ? request.getAdminRole()
                                                : null);

                // Ne modifier le mot de passe que si un nouveau mot de passe est fourni.
                if (request.getPassword() != null
                                && !request.getPassword().isBlank()) {

                        if (request.getPassword().length() < 8) {
                                throw new ResponseStatusException(
                                                HttpStatus.BAD_REQUEST,
                                                "Le mot de passe doit contenir au moins 8 caractères.");
                        }

                        user.setPassword(
                                        passwordEncoder.encode(request.getPassword()));
                }

                User savedUser = userRepository.save(user);

                return toAccountResponse(savedUser);
        }

        // =========================================================
        // MODIFIER UN MEMBRE
        // =========================================================

        @PutMapping("/members/{id}")
        public Member updateMember(
                        @PathVariable Long id,
                        @Valid @RequestBody Member memberDetails) {

                return memberService.updateMember(id, memberDetails);
        }

        // =========================================================
        // SUPPRIMER UNE FICHE MEMBRE
        // =========================================================

        @DeleteMapping("/members/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteMember(@PathVariable Long id) {

                memberService.deleteMember(id);
        }
}
