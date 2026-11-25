package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository;

import java.util.List;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
    List<Contribution> findByMemberId(Long memberId);
}
