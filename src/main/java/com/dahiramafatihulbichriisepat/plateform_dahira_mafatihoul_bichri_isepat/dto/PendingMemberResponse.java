package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto;

import java.time.LocalDate;

public record PendingMemberResponse(
        Long memberId,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String commission,
        String filiere,
        String promotion,
        LocalDate registrationDate) {
}
