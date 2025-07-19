package com.proj_chatBot.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.ServiceEntity;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {
}
