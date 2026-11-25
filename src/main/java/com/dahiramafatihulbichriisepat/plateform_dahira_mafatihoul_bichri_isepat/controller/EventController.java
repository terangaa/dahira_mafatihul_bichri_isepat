package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.controller;

import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.entity.EventEntity;
import com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service.EventService;
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
public class EventController {

    private final EventService eventService;

    @GetMapping("/events")
    public List<EventEntity> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/events/{id}")
    public EventEntity getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventEntity createEvent(@Valid @RequestBody EventEntity event) {
        return eventService.createEvent(event);
    }

    @PutMapping("/events/{id}")
    public EventEntity updateEvent(@PathVariable Long id, @Valid @RequestBody EventEntity eventDetails) {
        return eventService.updateEvent(id, eventDetails);
    }

    @DeleteMapping("/events/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
    }
}
