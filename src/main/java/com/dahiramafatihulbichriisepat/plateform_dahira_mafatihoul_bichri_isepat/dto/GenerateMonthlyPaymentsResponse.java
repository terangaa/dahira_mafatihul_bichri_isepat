package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenerateMonthlyPaymentsResponse {

    private int totalActiveMembers;
    private int created;
    private int alreadyExists;
    private Integer month;
    private Integer year;
}