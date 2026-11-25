
package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monthly_payments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_monthly_payment_member_period", columnNames = {
                "member_id",
                "payment_month",
                "payment_year"
        })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant du membre concerné.
     */
    @NotNull(message = "Le membre est obligatoire")
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    /**
     * Mois de la cotisation.
     * 1 = janvier
     * 2 = février
     * ...
     * 12 = décembre
     */
    @NotNull(message = "Le mois est obligatoire")
    @Column(name = "payment_month", nullable = false)
    private Integer month;

    /**
     * Année de la cotisation.
     */
    @NotNull(message = "L'année est obligatoire")
    @Column(name = "payment_year", nullable = false)
    private Integer year;

    /**
     * Montant attendu.
     */
    @Builder.Default
    @Column(name = "expected_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal expectedAmount = BigDecimal.ZERO;

    /**
     * Montant réellement payé.
     */
    @Builder.Default
    @Column(name = "paid_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Date du paiement.
     * Null si aucune somme n'a encore été payée.
     */
    @Column(name = "payment_date")
    private LocalDate paymentDate;

    /**
     * Statut calculé automatiquement :
     *
     * NON_PAYEE
     * PARTIEL
     * PAYE
     */
    @Builder.Default
    @Column(nullable = false, length = 30)
    private String status = "NON_PAYEE";

    /**
     * Mode de paiement :
     *
     * Wave
     * Orange Money
     * Mobile Money
     * Espèces
     * etc.
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * Observation du trésorier ou de l'administrateur.
     */
    @Column(length = 1000)
    private String observation;
}
