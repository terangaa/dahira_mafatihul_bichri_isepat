package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Member;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$")
    private String phone;

    private String commission;
    private String filiere;
    private String promotion;

    @NotNull
    private LocalDate registrationDate;

    @NotNull
    private Member.MemberStatus status = Member.MemberStatus.PENDING;

    @NotBlank
    @Email
    private String accountEmail;

    @NotBlank
    private String accountPassword;

    @NotNull
    private Role accountRole = Role.MEMBER;
}
