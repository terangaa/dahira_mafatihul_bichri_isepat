package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateMonthlyPaymentsRequest {

    @NotNull(message = "Le mois est obligatoire")
    @Min(value = 1, message = "Le mois doit être compris entre 1 et 12")
    @Max(value = 12, message = "Le mois doit être compris entre 1 et 12")
    private Integer month;

    @NotNull(message = "L'année est obligatoire")
    @Min(value = 2020, message = "Année invalide")
    private Integer year;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à 0")
    private BigDecimal amount;
}