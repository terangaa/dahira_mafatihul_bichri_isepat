package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.HistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryEntryRepository extends JpaRepository<HistoryEntry, Long> {
}
