
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.AuthRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.AuthResponse;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.RefreshRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.RegistrationResponse;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AccountStatus;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Member;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Role;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MemberRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.EmailNotificationService;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final UserRepository userRepository;
        private final MemberRepository memberRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final EmailNotificationService emailNotificationService;

        // ============================================================
        // INSCRIPTION
        // ============================================================

        @PostMapping("/register")
        @ResponseStatus(HttpStatus.CREATED)
        public RegistrationResponse register(
                        @Valid @RequestBody AuthRequest request,
                        @RequestHeader(name = "X-Charter-Accepted", required = false) String charterAccepted) {

                // L'acceptation de la charte est obligatoire.
                if (!"true".equalsIgnoreCase(charterAccepted)) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Vous devez lire et accepter la charte d'engagement avant de vous inscrire.");
                }

                // Vérifier l'adresse e-mail.
                String email = request.getEmail() == null
                                ? ""
                                : request.getEmail().trim();

                if (email.isBlank()) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "L'adresse e-mail est obligatoire.");
                }

                if (userRepository.existsByEmail(email)) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Cet e-mail est déjà utilisé.");
                }

                // Vérifier le numéro de téléphone.
                String phone = request.getPhone();

                if (phone != null && !phone.isBlank()) {
                        phone = phone.trim();

                        if (memberRepository.existsByPhone(phone)) {
                                throw new ResponseStatusException(
                                                HttpStatus.CONFLICT,
                                                "Ce numéro de téléphone est déjà utilisé.");
                        }
                }

                // Déterminer le prénom et le nom.
                String emailName = email.substring(0, email.indexOf('@'));

                String firstName = request.getFirstName() == null
                                || request.getFirstName().isBlank()
                                                ? emailName
                                                : request.getFirstName().trim();

                String lastName = request.getLastName() == null
                                || request.getLastName().isBlank()
                                                ? "Membre"
                                                : request.getLastName().trim();

                // Créer le compte utilisateur en attente.
                User user = User.builder()
                                .email(email)
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(Role.MEMBER)
                                .accountStatus(AccountStatus.PENDING)
                                .build();

                userRepository.save(user);

                // Générer un téléphone de remplacement si nécessaire.
                String memberPhone = phone;

                if (memberPhone == null || memberPhone.isBlank()) {
                        memberPhone = String.format("900000%04d", user.getId());

                        if (memberRepository.existsByPhone(memberPhone)) {
                                throw new ResponseStatusException(
                                                HttpStatus.CONFLICT,
                                                "Impossible de générer un numéro de téléphone de remplacement.");
                        }
                }

                // Créer la fiche membre.
                Member member = Member.builder()
                                .firstName(firstName)
                                .lastName(lastName)
                                .phone(memberPhone)
                                .commission(blankAsNull(request.getCommission()))
                                .filiere(blankAsNull(request.getFiliere()))
                                .promotion(blankAsNull(request.getPromotion()))
                                .registrationDate(LocalDate.now())
                                .status(Member.MemberStatus.PENDING)
                                .build();

                memberRepository.save(member);

                // Associer la fiche membre au compte utilisateur.
                user.setMemberId(member.getId());
                userRepository.save(user);

                // Notifier l'administrateur et le demandeur.
                // Le service d'e-mail gère ses propres erreurs d'envoi.
                emailNotificationService.notifyNewRegistration(
                                firstName + " " + lastName,
                                email);

                // Aucun JWT n'est délivré lors de l'inscription.
                return RegistrationResponse.builder()
                                .message(
                                                "Votre demande d'adhésion a bien été envoyée. "
                                                                + "Elle est en attente de validation par l'administration.")
                                .status(AccountStatus.PENDING.name())
                                .build();
        }

        // ============================================================
        // CONNEXION
        // ============================================================

        @PostMapping("/login")
        public AuthResponse login(@Valid @RequestBody AuthRequest request) {

                String email = request.getEmail() == null
                                ? ""
                                : request.getEmail().trim();

                if (email.isBlank() || request.getPassword() == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "L'adresse e-mail et le mot de passe sont obligatoires.");
                }

                // Rechercher le compte.
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "E-mail ou mot de passe incorrect."));

                // Vérifier le mot de passe avant de révéler le statut.
                if (!passwordEncoder.matches(
                                request.getPassword(),
                                user.getPassword())) {

                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "E-mail ou mot de passe incorrect.");
                }

                // Un compte en attente ne peut pas se connecter.
                if (user.getAccountStatus() == AccountStatus.PENDING) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Votre demande d'adhésion est en attente de validation par l'administration.");
                }

                // Un compte rejeté ne peut pas se connecter.
                if (user.getAccountStatus() == AccountStatus.REJECTED) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Votre demande d'adhésion n'a pas été retenue.");
                }

                // Seuls les comptes approuvés peuvent obtenir un JWT.
                if (user.getAccountStatus() != AccountStatus.APPROVED) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Votre compte n'est pas autorisé à se connecter.");
                }

                // Conserver la validation Spring Security.
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        email,
                                                        request.getPassword()));
                } catch (BadCredentialsException exception) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "E-mail ou mot de passe incorrect.");
                }

                // Générer les jetons uniquement après validation.
                String token = jwtService.generateToken(
                                user.getEmail(),
                                user.getRole().name());

                String refreshToken = jwtService.generateRefreshToken(
                                user.getEmail());

                return buildAuthResponse(token, refreshToken, user);
        }

        // ============================================================
        // RENOUVELLEMENT DU TOKEN
        // ============================================================

        @PostMapping("/refresh")
        public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {

                if (request.getRefreshToken() == null
                                || request.getRefreshToken().isBlank()) {

                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token manquant.");
                }

                String refreshToken = request.getRefreshToken();

                if (!jwtService.isRefreshTokenValid(refreshToken)) {
                        throw new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token invalide ou expiré.");
                }

                String email = jwtService.extractUsername(refreshToken);

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Utilisateur introuvable."));

                // Revérifier le statut à chaque renouvellement.
                if (user.getAccountStatus() != AccountStatus.APPROVED) {
                        throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Votre compte n'est pas approuvé. Le renouvellement du jeton est refusé.");
                }

                String newAccessToken = jwtService.generateToken(
                                user.getEmail(),
                                user.getRole().name());

                return buildAuthResponse(
                                newAccessToken,
                                refreshToken,
                                user);
        }

        // ============================================================
        // CONSTRUCTION DE LA RÉPONSE D'AUTHENTIFICATION
        // ============================================================

        private AuthResponse buildAuthResponse(
                        String token,
                        String refreshToken,
                        User user) {

                String adminRole = user.getAdminRole() == null
                                ? null
                                : user.getAdminRole().name();

                List<String> permissions = user.getAuthorities()
                                .stream()
                                .map(authority -> authority.getAuthority())
                                .filter(authority -> !authority.startsWith("ROLE_"))
                                .toList();

                return new AuthResponse(
                                token,
                                refreshToken,
                                user.getEmail(),
                                user.getRole().name(),
                                adminRole,
                                permissions);
        }

        // ============================================================
        // UTILITAIRE
        // ============================================================

        private String blankAsNull(String value) {
                return value == null || value.isBlank()
                                ? null
                                : value.trim();
        }
}
