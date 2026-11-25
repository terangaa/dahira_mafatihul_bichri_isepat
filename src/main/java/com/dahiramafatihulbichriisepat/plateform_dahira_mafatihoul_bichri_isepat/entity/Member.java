package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$", message = "Téléphone invalide")
    @Column(nullable = false, unique = true)
    private String phone;

    @Column(name = "filiere")
    private String filiere;

    @Column(name = "promotion")
    private String promotion;

    @Column(name = "commission")
    private String commission;

    @NotNull(message = "La date d'inscription est obligatoire")
    @Column(nullable = false)
    private LocalDate registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    public enum MemberStatus {
        ACTIVE,
        INACTIVE,
        PENDING
    }
}
