
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.AdminRole;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Role;

public record AdminAccountResponse(
        Long id,
        String email,
        Role role,
        AdminRole adminRole) {
}
