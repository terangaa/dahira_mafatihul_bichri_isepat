package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.config;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AccountStatus;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final UserDetailsService userDetailsService;

        @Override
        protected void doFilterInternal(
                        @NonNull HttpServletRequest request,
                        @NonNull HttpServletResponse response,
                        @NonNull FilterChain filterChain)
                        throws ServletException, IOException {

                final String authHeader = request.getHeader("Authorization");

                System.out.println("========== JWT FILTER ==========");
                System.out.println("URL : " + request.getRequestURI());
                System.out.println(
                                "Méthode : " + request.getMethod());
                System.out.println(
                                "Authorization présente : " + (authHeader != null));

                /*
                 * ============================================================
                 * 1. Vérification de l'en-tête Authorization
                 * ============================================================
                 */

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                        System.out.println("JWT : ABSENT");
                        System.out.println("================================");

                        filterChain.doFilter(request, response);
                        return;
                }

                /*
                 * ============================================================
                 * 2. Récupération du token
                 * ============================================================
                 */

                final String jwt = authHeader.substring(7).trim();

                if (jwt.isEmpty()) {

                        System.out.println("JWT : TOKEN VIDE");
                        System.out.println("================================");

                        filterChain.doFilter(request, response);
                        return;
                }

                try {

                        /*
                         * ========================================================
                         * 3. Extraction de l'email depuis le JWT
                         * ========================================================
                         */

                        String userEmail = jwtService.extractUsername(jwt);

                        System.out.println(
                                        "JWT : email extrait = " + userEmail);

                        /*
                         * ========================================================
                         * 4. Vérification qu'aucune authentification n'est déjà
                         * installée
                         * ========================================================
                         */

                        if (userEmail != null
                                        && SecurityContextHolder.getContext()
                                                        .getAuthentication() == null) {

                                /*
                                 * ====================================================
                                 * 5. Chargement de l'utilisateur depuis la base
                                 *
                                 * IMPORTANT :
                                 * C'est ici que les nouvelles permissions sont
                                 * récupérées depuis User.getAuthorities().
                                 * ====================================================
                                 */

                                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                                System.out.println(
                                                "Utilisateur trouvé : "
                                                                + userDetails.getUsername());

                                System.out.println(
                                                "Authorities utilisateur : "
                                                                + userDetails.getAuthorities());

                                /*
                                 * ====================================================
                                 * 6. Vérification de la validité du token
                                 * ====================================================
                                 */

                                boolean tokenValid = jwtService.isTokenValid(
                                                jwt,
                                                userDetails.getUsername());

                                if (!tokenValid) {

                                        System.out.println(
                                                        "JWT : TOKEN NON VALIDE");
                                        System.out.println(
                                                        "================================");

                                        SecurityContextHolder.clearContext();

                                        filterChain.doFilter(request, response);
                                        return;
                                }

                                if (!AccountStatus.APPROVED.equals(
                                                ((User) userDetails).getAccountStatus())) {

                                        System.out.println(
                                                        "JWT : COMPTE NON APPROUVE");

                                        SecurityContextHolder.clearContext();

                                        filterChain.doFilter(request, response);
                                        return;
                                }

                                /*
                                 * ====================================================
                                 * 7. Installation de l'authentification
                                 * ====================================================
                                 */

                                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities());

                                authToken.setDetails(
                                                new WebAuthenticationDetailsSource()
                                                                .buildDetails(request));

                                SecurityContextHolder.getContext()
                                                .setAuthentication(authToken);

                                /*
                                 * ====================================================
                                 * 8. Logs de vérification
                                 * ====================================================
                                 */

                                System.out.println(
                                                "AUTHENTIFICATION INSTALLEE");

                                System.out.println(
                                                "Authorities finales : "
                                                                + SecurityContextHolder
                                                                                .getContext()
                                                                                .getAuthentication()
                                                                                .getAuthorities());

                                System.out.println(
                                                "================================");
                        }

                } catch (JwtException | IllegalArgumentException exception) {

                        /*
                         * ========================================================
                         * Token expiré, invalide ou mal formé
                         * ========================================================
                         */

                        System.out.println(
                                        "ERREUR JWT : "
                                                        + exception.getClass()
                                                                        .getSimpleName());

                        System.out.println(
                                        "MESSAGE : "
                                                        + exception.getMessage());

                        SecurityContextHolder.clearContext();
                }

                filterChain.doFilter(request, response);
        }
}