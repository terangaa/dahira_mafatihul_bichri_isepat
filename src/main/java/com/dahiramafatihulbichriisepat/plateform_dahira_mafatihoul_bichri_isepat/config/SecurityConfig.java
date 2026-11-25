package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .csrf(csrf -> csrf.disable())

                                .headers(headers -> headers
                                                .frameOptions(frame -> frame.sameOrigin()))

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .formLogin(form -> form.disable())
                                .httpBasic(basic -> basic.disable())

                                .authorizeHttpRequests(auth -> auth

                                                // =====================================================
                                                // 1. RESSOURCES PUBLIQUES
                                                // =====================================================

                                                .requestMatchers(
                                                                "/",
                                                                "/home",
                                                                "/home.html",
                                                                "/login.html",
                                                                "/register.html",
                                                                "/error",
                                                                "/favicon.ico",
                                                                "/manifest.json",
                                                                "/service-worker.js",
                                                                "/image.png",
                                                                "/offline.html",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/uploads/**",
                                                                "/fonts/**",
                                                                "/webjars/**")
                                                .permitAll()

                                                // =====================================================
                                                // 2. AUTHENTIFICATION
                                                // =====================================================

                                                .requestMatchers("/api/auth/**").permitAll()

                                                // =====================================================
                                                // 3. CONSOLE H2
                                                // Désactiver en production
                                                // =====================================================

                                                .requestMatchers("/h2-console/**").permitAll()

                                                // =====================================================
                                                // 4. GESTION DES MEMBRES
                                                // =====================================================

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/members",
                                                                "/api/members/**")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/members")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/members/*")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/members/*")
                                                .permitAll()

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/members/*")
                                                .permitAll()

                                                // =====================================================
                                                // 5. DEMANDES D'ADHÉSION
                                                // =====================================================

                                                /*
                                                 * Consultation :
                                                 * ADMINISTRATEUR
                                                 * TRESORIER
                                                 * ADMIN_PRINCIPAL
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/admin/membership-requests",
                                                                "/api/admin/membership-requests/**")
                                                .hasAnyRole(
                                                                "ADMINISTRATEUR",
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                /*
                                                 * Acceptation :
                                                 * TRESORIER
                                                 * ADMIN_PRINCIPAL
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/admin/membership-requests/*/approve")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                /*
                                                 * Refus :
                                                 * TRESORIER
                                                 * ADMIN_PRINCIPAL
                                                 */

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/admin/membership-requests/*/reject")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // =====================================================
                                                // 6. FINANCES
                                                // =====================================================

                                                // Consultation financière

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/admin/finances",
                                                                "/api/admin/finances/**")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL",
                                                                "ADMINISTRATEUR",
                                                                "ADMIN")

                                                // Suivi des cotisations et contributions

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/monthly-payments",
                                                                "/api/monthly-payments/**",
                                                                "/api/contributions",
                                                                "/api/contributions/**")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL",
                                                                "ADMINISTRATEUR",
                                                                "ADMIN")

                                                // Création

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/monthly-payments",
                                                                "/api/monthly-payments/**",
                                                                "/api/contributions",
                                                                "/api/contributions/**")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // Modification

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/monthly-payments/**",
                                                                "/api/contributions/**")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/monthly-payments/**",
                                                                "/api/contributions/**")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // Annulation

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/monthly-payments/*/cancel",
                                                                "/api/contributions/*/cancel")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // Génération automatique

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/monthly-payments/generate")
                                                .hasAnyRole(
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // Suppression financière interdite

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/monthly-payments/**",
                                                                "/api/contributions/**")
                                                .denyAll()

                                                // Audit financier

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/admin/finance-audit",
                                                                "/api/admin/finance-audit/**")
                                                .hasRole("ADMIN_PRINCIPAL")

                                                // Finances personnelles

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/my-finances",
                                                                "/api/my-finances/**",
                                                                "/api/my-monthly-payments",
                                                                "/api/my-monthly-payments/**",
                                                                "/api/my-contributions",
                                                                "/api/my-contributions/**")
                                                .hasAnyRole(
                                                                "MEMBER",
                                                                "ADMINISTRATEUR",
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // =====================================================
                                                // 7. AUTRES MODULES
                                                // =====================================================

                                                // Lecture

                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/commissions",
                                                                "/api/commissions/**",
                                                                "/api/events",
                                                                "/api/events/**",
                                                                "/api/projects",
                                                                "/api/projects/**",
                                                                "/api/history",
                                                                "/api/history/**")
                                                .hasAnyRole(
                                                                "MEMBER",
                                                                "ADMINISTRATEUR",
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // Création

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/commissions",
                                                                "/api/commissions/**",
                                                                "/api/events",
                                                                "/api/events/**",
                                                                "/api/projects",
                                                                "/api/projects/**")
                                                .hasAnyRole(
                                                                "ADMINISTRATEUR",
                                                                "ADMIN_PRINCIPAL")

                                                // Modification

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/commissions/**",
                                                                "/api/events/**",
                                                                "/api/projects/**")
                                                .hasAnyRole(
                                                                "ADMINISTRATEUR",
                                                                "ADMIN_PRINCIPAL")

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/commissions/**",
                                                                "/api/events/**",
                                                                "/api/projects/**")
                                                .hasAnyRole(
                                                                "ADMINISTRATEUR",
                                                                "ADMIN_PRINCIPAL")

                                                // Suppression

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/commissions/**",
                                                                "/api/events/**",
                                                                "/api/projects/**")
                                                .hasAnyRole(
                                                                "ADMINISTRATEUR",
                                                                "ADMIN_PRINCIPAL")

                                                // Historique : écriture interdite

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/history",
                                                                "/api/history/**")
                                                .denyAll()

                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/history",
                                                                "/api/history/**")
                                                .denyAll()

                                                .requestMatchers(
                                                                HttpMethod.PATCH,
                                                                "/api/history",
                                                                "/api/history/**")
                                                .denyAll()

                                                .requestMatchers(
                                                                HttpMethod.DELETE,
                                                                "/api/history",
                                                                "/api/history/**")
                                                .denyAll()

                                                // =====================================================
                                                // 8. PROFIL PERSONNEL
                                                // =====================================================

                                                .requestMatchers(
                                                                "/api/my-profile",
                                                                "/api/my-profile/**")
                                                .hasAnyRole(
                                                                "MEMBER",
                                                                "ADMINISTRATEUR",
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // =====================================================
                                                // 9. ADMINISTRATION DES AUTRES COMPTES
                                                // =====================================================

                                                /*
                                                 * Les demandes d'adhésion ont déjà leurs propres
                                                 * permissions définies plus haut.
                                                 *
                                                 * Les autres routes /api/admin/** restent réservées
                                                 * à ADMIN_PRINCIPAL.
                                                 */

                                                .requestMatchers("/api/admin/**")
                                                .hasRole("ADMIN_PRINCIPAL")

                                                // =====================================================
                                                // 10. AUTRES ROUTES API
                                                // =====================================================

                                                .requestMatchers("/api/**")
                                                .hasAnyRole(
                                                                "ADMIN",
                                                                "MEMBER",
                                                                "ADMINISTRATEUR",
                                                                "TRESORIER",
                                                                "ADMIN_PRINCIPAL")

                                                // =====================================================
                                                // 11. AUTRES PAGES
                                                // =====================================================

                                                .anyRequest().permitAll())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // =====================================================
        // PASSWORD ENCODER
        // =====================================================

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        // =====================================================
        // AUTHENTICATION MANAGER
        // =====================================================

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration authenticationConfiguration) throws Exception {

                return authenticationConfiguration.getAuthenticationManager();
        }
}