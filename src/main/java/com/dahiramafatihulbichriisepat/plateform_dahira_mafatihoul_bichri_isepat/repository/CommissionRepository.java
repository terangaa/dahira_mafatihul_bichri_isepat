package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Commission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {
}
