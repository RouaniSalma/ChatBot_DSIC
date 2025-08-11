package com.proj_chatBot.backend.repository;
import com.proj_chatBot.backend.entities.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.Participant;

import java.util.List;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByEvenement(Evenement evenement);
    long countByEvenement(Evenement evenement);
}
