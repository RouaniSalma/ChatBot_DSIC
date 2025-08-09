package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.Division;
import com.proj_chatBot.backend.repository.DivisionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/divisions")
public class DivisionController {

    private final DivisionRepository divisionRepository;

    public DivisionController(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    @GetMapping
    public ResponseEntity<List<Division>> getAllDivisions() {
        return ResponseEntity.ok(divisionRepository.findAll());
    }
}
