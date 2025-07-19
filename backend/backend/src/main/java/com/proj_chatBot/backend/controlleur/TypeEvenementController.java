package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.service.TypeEvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/types-evenement")
public class TypeEvenementController {

    @Autowired
    private TypeEvenementService typeEvenementService;

    @GetMapping
    public List<TypeEvenement> getAllTypesEvenement() {
        return typeEvenementService.getAllTypesEvenement();
    }

    @GetMapping("/{id}")
    public Optional<TypeEvenement> getTypeEvenementById(@PathVariable Long id) {
        return typeEvenementService.getTypeEvenementById(id);
    }

    @PostMapping
    public TypeEvenement createTypeEvenement(@RequestBody TypeEvenement typeEvenement) {
        return typeEvenementService.createTypeEvenement(typeEvenement);
    }
}
