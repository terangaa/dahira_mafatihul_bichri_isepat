
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.security.Permission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Email invalide")
        @Column(nullable = false, unique = true)
        private String email;

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        @Column(nullable = false)
        private String password;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        @Builder.Default
        private Role role = Role.MEMBER;

        @Enumerated(EnumType.STRING)
        @Column(name = "admin_role")
        private AdminRole adminRole;

        @Enumerated(EnumType.STRING)
        @Column(name = "account_status", nullable = false)
        @Builder.Default
        private AccountStatus accountStatus = AccountStatus.PENDING;

        @Column(name = "member_id")
        private Long memberId;

        // ============================================================
        // AUTHORITIES / PERMISSIONS
        // ============================================================

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {

                List<GrantedAuthority> authorities = new ArrayList<>();

                // Vérifier que le rôle principal existe
                if (role == null) {
                        return authorities;
                }

                // Rôle principal
                authorities.add(
                                new SimpleGrantedAuthority("ROLE_" + role.name()));

                // Les membres n'ont aucune permission administrative
                if (role != Role.ADMIN || adminRole == null) {
                        return authorities;
                }

                // Ajouter le sous-rôle administratif
                authorities.add(
                                new SimpleGrantedAuthority("ROLE_" + adminRole.name()));

                switch (adminRole) {

                        // ====================================================
                        // ADMINISTRATEUR PRINCIPAL
                        // ====================================================

                        case ADMIN_PRINCIPAL -> {

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_READ.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_CREATE.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_UPDATE.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_GENERATE.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_CANCEL.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_AUDIT.name()));
                        }

                        // ====================================================
                        // TRÉSORIER
                        // ====================================================

                        case TRESORIER -> {

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_READ.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_CREATE.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_UPDATE.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_GENERATE.name()));

                                authorities.add(new SimpleGrantedAuthority(
                                                Permission.FINANCE_CANCEL.name()));
                        }

                        // ====================================================
                        // ADMINISTRATEUR CLASSIQUE
                        // ====================================================

                        case ADMINISTRATEUR -> {
                                // Aucun accès financier.
                                // Il conserve uniquement ses autorisations
                                // administratives générales.
                        }
                }

                return authorities;
        }

        @Override
        public String getUsername() {
                return email;
        }

        @Override
        public boolean isAccountNonExpired() {
                return true;
        }

        @Override
        public boolean isAccountNonLocked() {
                return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
                return true;
        }

        @Override
        public boolean isEnabled() {
                return accountStatus == AccountStatus.APPROVED;
        }
}
