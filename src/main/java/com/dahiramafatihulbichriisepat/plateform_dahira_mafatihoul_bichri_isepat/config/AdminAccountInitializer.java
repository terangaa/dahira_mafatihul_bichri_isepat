
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.config;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AccountStatus;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Role;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@dahira.test}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        System.out.println("==========================================");
        System.out.println("       ADMIN ACCOUNT INITIALIZER");
        System.out.println("==========================================");
        System.out.println("Email configuré : " + adminEmail);
        System.out.println("Recherche du compte administrateur...");

        User admin = userRepository.findByEmail(adminEmail).orElse(null);

        if (admin != null) {

            System.out.println("Compte administrateur trouvé.");
            System.out.println("ID   : " + admin.getId());
            System.out.println("Email: " + admin.getEmail());

            admin.setRole(Role.ADMIN);
            admin.setAccountStatus(AccountStatus.APPROVED);
            admin.setPassword(passwordEncoder.encode(adminPassword));

            userRepository.save(admin);

            System.out.println("Rôle défini : ADMIN");
            System.out.println("Mot de passe administrateur réinitialisé.");
            System.out.println("Connexion prévue avec : " + adminEmail);
            System.out.println("==========================================");

            return;
        }

        User newAdmin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .accountStatus(AccountStatus.APPROVED)
                .build();

        userRepository.save(newAdmin);

        System.out.println("Aucun compte administrateur trouvé.");
        System.out.println("Nouveau compte administrateur créé.");
        System.out.println("Email : " + adminEmail);
        System.out.println("Rôle  : ADMIN");
        System.out.println("==========================================");
    }
}
