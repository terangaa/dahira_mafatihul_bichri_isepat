package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.FinanceSummary;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Contribution;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.MonthlyPayment;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.User;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.ContributionRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MonthlyPaymentRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.UserRepository;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FinanceController {

    private final MonthlyPaymentRepository monthlyPaymentRepository;
    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;

    @GetMapping("/my-finances")
    public FinanceSummary getMyFinances(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        Long memberId = user.getMemberId();
        if (memberId == null) {
            throw new AccessDeniedException("Aucune donnée financière associée à ce compte");
        }

        List<MonthlyPayment> monthlyPayments = monthlyPaymentRepository.findAll().stream()
                .filter(payment -> payment.getMemberId().equals(memberId))
                .toList();

        List<Contribution> contributions = contributionRepository.findAll().stream()
                .filter(contribution -> contribution.getMemberId().equals(memberId))
                .toList();

        BigDecimal totalMonthlyPayments = monthlyPayments.stream()
                .map(MonthlyPayment::getExpectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = monthlyPayments.stream()
                .map(MonthlyPayment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalMonthlyPayments.subtract(totalPaid);

        BigDecimal totalContributions = contributions.stream()
                .map(Contribution::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinanceSummary.builder()
                .memberId(memberId)
                .totalMonthlyPayments(totalMonthlyPayments)
                .totalPaid(totalPaid)
                .totalRemaining(totalRemaining)
                .totalContributions(totalContributions)
                .build();
    }

    @GetMapping("/admin/finances")
    public List<FinanceSummary> getAdminFinances(Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Accès refusé");
        }

        return userRepository.findAll().stream()
                .filter(user -> user.getMemberId() != null)
                .map(user -> getFinanceSummaryForMember(user.getMemberId()))
                .toList();
    }

    @GetMapping("/admin/finances/{memberId}")
    public FinanceSummary getAdminMemberFinance(@PathVariable Long memberId, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Accès refusé");
        }
        return getFinanceSummaryForMember(memberId);
    }

    private FinanceSummary getFinanceSummaryForMember(Long memberId) {
        List<MonthlyPayment> monthlyPayments = monthlyPaymentRepository.findAll().stream()
                .filter(payment -> payment.getMemberId().equals(memberId))
                .toList();

        List<Contribution> contributions = contributionRepository.findAll().stream()
                .filter(contribution -> contribution.getMemberId().equals(memberId))
                .toList();

        BigDecimal totalMonthlyPayments = monthlyPayments.stream()
                .map(MonthlyPayment::getExpectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPaid = monthlyPayments.stream()
                .map(MonthlyPayment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalMonthlyPayments.subtract(totalPaid);

        BigDecimal totalContributions = contributions.stream()
                .map(Contribution::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinanceSummary.builder()
                .memberId(memberId)
                .totalMonthlyPayments(totalMonthlyPayments)
                .totalPaid(totalPaid)
                .totalRemaining(totalRemaining)
                .totalContributions(totalContributions)
                .build();
    }
}
