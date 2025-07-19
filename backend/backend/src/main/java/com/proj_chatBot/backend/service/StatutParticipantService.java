package com.proj_chatBot.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.proj_chatBot.backend.entities.StatutParticipant;
import com.proj_chatBot.backend.repository.StatutParticipantRepository;

@Service
public class StatutParticipantService {

    @Autowired
    private StatutParticipantRepository statutParticipantRepository;

    public List<StatutParticipant> getAllStatutsParticipant() {
        return statutParticipantRepository.findAll();
    }

    public Optional<StatutParticipant> getStatutParticipantById(Long id) {
        return statutParticipantRepository.findById(id);
    }

    public StatutParticipant createStatutParticipant(StatutParticipant statutParticipant) {
        return statutParticipantRepository.save(statutParticipant);
    }
}

