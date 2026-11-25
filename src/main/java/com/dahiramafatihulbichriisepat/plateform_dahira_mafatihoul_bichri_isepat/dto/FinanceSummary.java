package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceSummary {
    private Long memberId;
    private BigDecimal totalMonthlyPayments;
    private BigDecimal totalPaid;
    private BigDecimal totalRemaining;
    private BigDecimal totalContributions;
}
