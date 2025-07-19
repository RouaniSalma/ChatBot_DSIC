package com.proj_chatBot.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long>{
}
