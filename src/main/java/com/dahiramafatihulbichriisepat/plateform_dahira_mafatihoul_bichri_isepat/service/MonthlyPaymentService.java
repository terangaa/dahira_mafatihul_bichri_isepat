package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.GenerateMonthlyPaymentsRequest;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.dto.GenerateMonthlyPaymentsResponse;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Member;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.MonthlyPayment;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MemberRepository;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.MonthlyPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyPaymentService {

        private final MonthlyPaymentRepository monthlyPaymentRepository;
        private final MemberRepository memberRepository;

        // =========================================================
        // LECTURE
        // =========================================================

        public List<MonthlyPayment> getAllMonthlyPayments() {
                return monthlyPaymentRepository.findAll();
        }

        public MonthlyPayment getMonthlyPaymentById(Long id) {
                return monthlyPaymentRepository.findById(id)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Cotisation introuvable."));
        }

        public List<MonthlyPayment> getMonthlyPaymentsByMemberId(Long memberId) {
                return monthlyPaymentRepository.findByMemberId(memberId);
        }

        public List<MonthlyPayment> getMonthlyPaymentsByPeriod(Integer month, Integer year) {
                return monthlyPaymentRepository.findByMonthAndYear(month, year);
        }

        // =========================================================
        // CRÉATION
        // =========================================================

        @Transactional
        public MonthlyPayment createMonthlyPayment(MonthlyPayment payment) {

                // 1. Vérifications des champs obligatoires
                if (payment.getMemberId() == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "Le membre est obligatoire.");
                }

                if (payment.getMonth() == null || payment.getYear() == null) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "Le mois et l'année sont obligatoires.");
                }

                validateExpectedAmount(payment.getExpectedAmount());

                // 2. Vérifier que le membre existe
                Member member = memberRepository.findById(payment.getMemberId())
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Membre introuvable."));

                // 3. ✅ Vérifier qu'il n'existe pas déjà une cotisation pour ce membre + mois +
                // année
                boolean exists = monthlyPaymentRepository.existsByMemberIdAndMonthAndYear(
                                payment.getMemberId(),
                                payment.getMonth(),
                                payment.getYear());

                if (exists) {
                        throw new ResponseStatusException(
                                        HttpStatus.CONFLICT,
                                        "Une cotisation existe déjà pour ce membre sur cette période.");
                }

                // 4. Calcul automatique du statut
                payment.setStatus(calculateStatus(
                                payment.getExpectedAmount(),
                                payment.getPaidAmount()));

                // 5. Date de paiement par défaut si montant payé > 0
                if (payment.getPaidAmount() != null
                                && payment.getPaidAmount().compareTo(BigDecimal.ZERO) > 0
                                && payment.getPaymentDate() == null) {
                        payment.setPaymentDate(LocalDate.now());
                }

                return monthlyPaymentRepository.save(payment);
        }

        // =========================================================
        // MODIFICATION
        // =========================================================

        @Transactional
        public MonthlyPayment updateMonthlyPayment(Long id, MonthlyPayment details) {

                MonthlyPayment existing = getMonthlyPaymentById(id);

                // Vérifier si le changement de période crée un doublon
                if (!existing.getMemberId().equals(details.getMemberId())
                                || !existing.getMonth().equals(details.getMonth())
                                || !existing.getYear().equals(details.getYear())) {

                        boolean exists = monthlyPaymentRepository.existsByMemberIdAndMonthAndYear(
                                        details.getMemberId(),
                                        details.getMonth(),
                                        details.getYear());

                        if (exists) {
                                throw new ResponseStatusException(
                                                HttpStatus.CONFLICT,
                                                "Une autre cotisation existe déjà pour ce membre sur cette période.");
                        }
                }

                validateExpectedAmount(details.getExpectedAmount());

                existing.setMemberId(details.getMemberId());
                existing.setMonth(details.getMonth());
                existing.setYear(details.getYear());
                existing.setExpectedAmount(details.getExpectedAmount());
                existing.setPaidAmount(details.getPaidAmount());
                existing.setPaymentDate(details.getPaymentDate());
                existing.setPaymentMethod(details.getPaymentMethod());
                existing.setObservation(details.getObservation());
                existing.setStatus(calculateStatus(
                                details.getExpectedAmount(),
                                details.getPaidAmount()));

                return monthlyPaymentRepository.save(existing);
        }

        // =========================================================
        // ANNULATION
        // =========================================================

        @Transactional
        public MonthlyPayment cancelMonthlyPayment(Long id) {
                MonthlyPayment payment = getMonthlyPaymentById(id);
                payment.setStatus("ANNULEE");
                payment.setPaidAmount(BigDecimal.ZERO);
                return monthlyPaymentRepository.save(payment);
        }

        // =========================================================
        // GÉNÉRATION AUTOMATIQUE
        // =========================================================

        @Transactional
        public GenerateMonthlyPaymentsResponse generateMonthlyPayments(
                        GenerateMonthlyPaymentsRequest request) {

                List<Member> members = memberRepository.findAll();

                int created = 0;
                int alreadyExists = 0;

                for (Member member : members) {
                        boolean exists = monthlyPaymentRepository.existsByMemberIdAndMonthAndYear(
                                        member.getId(),
                                        request.getMonth(),
                                        request.getYear());

                        if (exists) {
                                alreadyExists++;
                                continue;
                        }

                        MonthlyPayment payment = MonthlyPayment.builder()
                                        .memberId(member.getId())
                                        .month(request.getMonth())
                                        .year(request.getYear())
                                        .expectedAmount(request.getAmount())
                                        .paidAmount(BigDecimal.ZERO)
                                        .status("NON_PAYEE")
                                        .build();

                        monthlyPaymentRepository.save(payment);
                        created++;
                }

                return new GenerateMonthlyPaymentsResponse(
                                members.size(),
                                created,
                                alreadyExists,
                                request.getMonth(),
                                request.getYear());
        }

        // =========================================================
        // VALIDATIONS
        // =========================================================

        private void validateExpectedAmount(BigDecimal amount) {
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Le montant attendu doit être supérieur à 0.");
                }
        }

        private String calculateStatus(BigDecimal expected, BigDecimal paid) {
                if (paid == null || paid.compareTo(BigDecimal.ZERO) == 0) {
                        return "NON_PAYEE";
                }
                if (paid.compareTo(expected) >= 0) {
                        return "PAYE";
                }
                return "PARTIEL";
        }
}