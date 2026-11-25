package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.ProjectEntity;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectEntity> getAllProjects() {
        return projectRepository.findAll();
    }

    public ProjectEntity getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Projet introuvable avec l'id : " + id));
    }

    @Transactional
    public ProjectEntity createProject(ProjectEntity project) {
        return projectRepository.save(project);
    }

    @Transactional
    public ProjectEntity updateProject(Long id, ProjectEntity projectDetails) {
        ProjectEntity project = getProjectById(id);
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setStartDate(projectDetails.getStartDate());
        project.setEndDate(projectDetails.getEndDate());
        project.setPlannedBudget(projectDetails.getPlannedBudget());
        project.setCollectedAmount(projectDetails.getCollectedAmount());
        project.setStatus(projectDetails.getStatus());
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.delete(getProjectById(id));
    }
}
