package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contributions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le membre est obligatoire")
    @Column(nullable = false)
    private Long memberId;

    @Column
    private String eventOrProjectName;

    @Column(precision = 19, scale = 2)
    private BigDecimal requestedAmount;

    @Column(precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @Column
    private LocalDate contributionDate;

    @Column(nullable = false)
    @Builder.Default
    private String status = "EN_ATTENTE";

    @Column
    private String paymentMethod;

    @Column(length = 1000)
    private String observation;
}
