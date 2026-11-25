package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Commission;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.CommissionService;
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
public class CommissionController {

    private final CommissionService commissionService;

    @GetMapping("/commissions")
    public List<Commission> getAllCommissions() {
        return commissionService.getAllCommissions();
    }

    @GetMapping("/commissions/{id}")
    public Commission getCommissionById(@PathVariable Long id) {
        return commissionService.getCommissionById(id);
    }

    @PostMapping("/commissions")
    @ResponseStatus(HttpStatus.CREATED)
    public Commission createCommission(@Valid @RequestBody Commission commission) {
        return commissionService.createCommission(commission);
    }

    @PutMapping("/commissions/{id}")
    public Commission updateCommission(@PathVariable Long id, @Valid @RequestBody Commission commissionDetails) {
        return commissionService.updateCommission(id, commissionDetails);
    }

    @DeleteMapping("/commissions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommission(@PathVariable Long id) {
        commissionService.deleteCommission(id);
    }
}
