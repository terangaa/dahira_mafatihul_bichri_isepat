
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AdminRole;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminAccountUpdateRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'adresse email est invalide")
    private String email;

    // Facultatif lors de la modification
    private String password;

    @NotNull(message = "Le rôle principal est obligatoire")
    private Role role;

    // Sous-rôle administratif facultatif pour les membres
    private AdminRole adminRole;
}
