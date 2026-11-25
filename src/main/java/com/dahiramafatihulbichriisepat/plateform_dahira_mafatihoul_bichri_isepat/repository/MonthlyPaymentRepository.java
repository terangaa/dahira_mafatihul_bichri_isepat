package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.MonthlyPayment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Long> {

    boolean existsByMemberIdAndMonthAndYear(
            Long memberId,
            Integer month,
            Integer year);

    List<MonthlyPayment> findByMemberId(Long memberId);

    List<MonthlyPayment> findByMonthAndYear(
            Integer month,
            Integer year);
}