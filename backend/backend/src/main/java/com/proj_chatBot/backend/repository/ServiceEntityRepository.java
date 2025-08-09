package com.proj_chatBot.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proj_chatBot.backend.entities.ServiceEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {
    @Query("SELECT s FROM ServiceEntity s WHERE s.division.idDivision = :divisionId")
    List<ServiceEntity> findByDivisionId(@Param("divisionId") Long divisionId);
}