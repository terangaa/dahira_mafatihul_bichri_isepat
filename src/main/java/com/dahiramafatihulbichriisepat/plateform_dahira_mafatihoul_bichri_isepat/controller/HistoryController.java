package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.HistoryEntry;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.HistoryEntryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryEntryService historyEntryService;

    @GetMapping("/history")
    public List<HistoryEntry> getAllHistoryEntries() {
        return historyEntryService.getAllHistoryEntries();
    }

    @GetMapping("/history/{id}")
    public HistoryEntry getHistoryEntryById(@PathVariable Long id) {
        return historyEntryService.getHistoryEntryById(id);
    }

    @PostMapping("/history")
    @ResponseStatus(HttpStatus.CREATED)
    public HistoryEntry createHistoryEntry(@Valid @RequestBody HistoryEntry entry) {
        return historyEntryService.createHistoryEntry(entry);
    }

    @PutMapping("/history/{id}")
    public HistoryEntry updateHistoryEntry(@PathVariable Long id, @Valid @RequestBody HistoryEntry entryDetails) {
        return historyEntryService.updateHistoryEntry(id, entryDetails);
    }

    @DeleteMapping("/history/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHistoryEntry(@PathVariable Long id) {
        historyEntryService.deleteHistoryEntry(id);
    }
}
