package com.proj_chatBot.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.proj_chatBot.backend.entities.ServiceEntity;
import com.proj_chatBot.backend.repository.ServiceEntityRepository;

@Service
public class ServiceEntityService {

    @Autowired
    private ServiceEntityRepository serviceEntityRepository;

    public List<ServiceEntity> getAllServices() {
        return serviceEntityRepository.findAll();
    }

    public Optional<ServiceEntity> getServiceById(Long id) {
        return serviceEntityRepository.findById(id);
    }

    public ServiceEntity createService(ServiceEntity serviceEntity) {
        return serviceEntityRepository.save(serviceEntity);
    }
}
