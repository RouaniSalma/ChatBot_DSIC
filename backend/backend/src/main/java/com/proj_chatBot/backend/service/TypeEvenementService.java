package com.proj_chatBot.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.proj_chatBot.backend.entities.TypeEvenement;
import com.proj_chatBot.backend.repository.TypeEvenementRepository;

@Service
public class TypeEvenementService {

    @Autowired
    private TypeEvenementRepository typeEvenementRepository;

    public List<TypeEvenement> getAllTypesEvenement() {
        return typeEvenementRepository.findAll();
    }

    public Optional<TypeEvenement> getTypeEvenementById(Long id) {
        return typeEvenementRepository.findById(id);
    }

    public TypeEvenement createTypeEvenement(TypeEvenement typeEvenement) {
        return typeEvenementRepository.save(typeEvenement);
    }
}
