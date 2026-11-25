package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.Commission;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.CommissionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRepository commissionRepository;

    public List<Commission> getAllCommissions() {
        return commissionRepository.findAll();
    }

    public Commission getCommissionById(Long id) {
        return commissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commission introuvable avec l'id : " + id));
    }

    @Transactional
    public Commission createCommission(Commission commission) {
        return commissionRepository.save(commission);
    }

    @Transactional
    public Commission updateCommission(Long id, Commission commissionDetails) {
        Commission commission = getCommissionById(id);
        commission.setName(commissionDetails.getName());
        commission.setDescription(commissionDetails.getDescription());
        return commissionRepository.save(commission);
    }

    @Transactional
    public void deleteCommission(Long id) {
        Commission commission = getCommissionById(id);
        commissionRepository.delete(commission);
    }
}
