package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.EventEntity;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.repository.EventRepository;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Récupérer tous les événements.
     */
    @Transactional(readOnly = true)
    public List<EventEntity> getAllEvents() {
        return eventRepository.findAll();
    }

    /**
     * Récupérer un événement par son identifiant.
     */
    @Transactional(readOnly = true)
    public EventEntity getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Événement introuvable avec l'id : " + id));
    }

    /**
     * Créer un événement.
     */
    @Transactional
    public EventEntity createEvent(EventEntity event) {

        // Affectation explicite du statut par défaut.
        event.setStatus("PLANNED");

        System.out.println("========== CRÉATION ÉVÉNEMENT ==========");
        System.out.println("Nom : " + event.getName());
        System.out.println("Date : " + event.getEventDate());
        System.out.println("Statut avant sauvegarde : " + event.getStatus());
        System.out.println("========================================");

        return eventRepository.save(event);
    }

    /**
     * Modifier un événement existant.
     */
    @Transactional
    public EventEntity updateEvent(Long id, EventEntity eventDetails) {

        EventEntity event = getEventById(id);

        event.setName(eventDetails.getName());
        event.setDescription(eventDetails.getDescription());
        event.setEventDate(eventDetails.getEventDate());
        event.setLocation(eventDetails.getLocation());
        event.setBudget(eventDetails.getBudget());

        // Ne jamais écraser un statut existant avec NULL ou une chaîne vide.
        if (eventDetails.getStatus() != null
                && !eventDetails.getStatus().isBlank()) {

            event.setStatus(eventDetails.getStatus());
        }

        // Sécurité supplémentaire.
        if (event.getStatus() == null || event.getStatus().isBlank()) {
            event.setStatus("PLANNED");
        }

        return eventRepository.save(event);
    }

    /**
     * Supprimer un événement.
     */
    @Transactional
    public void deleteEvent(Long id) {
        eventRepository.delete(getEventById(id));
    }
}