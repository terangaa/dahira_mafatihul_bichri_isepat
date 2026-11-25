package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.CommissionRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.ContributionRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.EventRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MemberRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MonthlyPaymentRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.ProjectRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final MemberRepository memberRepository;
    private final CommissionRepository commissionRepository;
    private final EventRepository eventRepository;
    private final ProjectRepository projectRepository;
    private final MonthlyPaymentRepository monthlyPaymentRepository;
    private final ContributionRepository contributionRepository;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("membersCount", memberRepository.count());
        stats.put("commissionsCount", commissionRepository.count());
        stats.put("eventsCount", eventRepository.count());
        stats.put("projectsCount", projectRepository.count());

        BigDecimal collectedMonthly = monthlyPaymentRepository.findAll().stream()
                .map(payment -> payment.getPaidAmount() == null ? BigDecimal.ZERO : payment.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingMonthly = monthlyPaymentRepository.findAll().stream()
                .map(payment -> payment.getExpectedAmount() == null ? BigDecimal.ZERO : payment.getExpectedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(collectedMonthly);

        BigDecimal collectedContributions = contributionRepository.findAll().stream()
                .map(contribution -> contribution.getPaidAmount() == null ? BigDecimal.ZERO : contribution.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingContributions = contributionRepository.findAll().stream()
                .map(contribution -> contribution.getRequestedAmount() == null ? BigDecimal.ZERO : contribution.getRequestedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(collectedContributions);

        stats.put("collectedMonthlyPayments", collectedMonthly);
        stats.put("remainingMonthlyPayments", remainingMonthly);
        stats.put("collectedContributions", collectedContributions);
        stats.put("remainingContributions", remainingContributions);
        stats.put("totalFinancial", collectedMonthly.add(collectedContributions));

        return stats;
    }
}
