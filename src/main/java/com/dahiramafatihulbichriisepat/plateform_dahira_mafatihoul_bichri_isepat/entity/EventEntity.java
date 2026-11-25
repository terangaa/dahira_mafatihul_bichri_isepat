package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de l'événement est obligatoire")
    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @NotNull(message = "La date de l'événement est obligatoire")
    @Column(nullable = false)
    private LocalDate eventDate;

    @Column
    private String location;

    @Column(precision = 19, scale = 2)
    private BigDecimal budget;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PLANNED";
}
