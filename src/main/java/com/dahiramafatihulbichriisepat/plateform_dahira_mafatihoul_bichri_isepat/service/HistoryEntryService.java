package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.HistoryEntry;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.HistoryEntryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HistoryEntryService {

    private final HistoryEntryRepository historyEntryRepository;

    public List<HistoryEntry> getAllHistoryEntries() {
        return historyEntryRepository.findAll();
    }

    public HistoryEntry getHistoryEntryById(Long id) {
        return historyEntryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Historique introuvable avec l'id : " + id));
    }

    @Transactional
    public HistoryEntry createHistoryEntry(HistoryEntry entry) {
        return historyEntryRepository.save(entry);
    }

    @Transactional
    public HistoryEntry updateHistoryEntry(Long id, HistoryEntry entryDetails) {
        HistoryEntry entry = getHistoryEntryById(id);
        entry.setYear(entryDetails.getYear());
        entry.setTitle(entryDetails.getTitle());
        entry.setDescription(entryDetails.getDescription());
        entry.setEventDate(entryDetails.getEventDate());
        return historyEntryRepository.save(entry);
    }

    @Transactional
    public void deleteHistoryEntry(Long id) {
        historyEntryRepository.delete(getHistoryEntryById(id));
    }
}
