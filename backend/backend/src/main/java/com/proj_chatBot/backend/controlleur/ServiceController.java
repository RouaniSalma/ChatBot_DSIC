package com.proj_chatBot.backend.controlleur;

import com.proj_chatBot.backend.entities.ServiceEntity;
import com.proj_chatBot.backend.repository.ServiceEntityRepository;
import com.proj_chatBot.backend.service.ServiceEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceEntityRepository serviceRepository;

    public ServiceController(ServiceEntityRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        return ResponseEntity.ok(serviceRepository.findAll());
    }

    @GetMapping("/by-division/{divisionId}")
    public ResponseEntity<List<ServiceEntity>> getServicesByDivision(@PathVariable Long divisionId) {
        System.out.println("Requête pour les services de la division: " + divisionId);
        List<ServiceEntity> services = serviceRepository.findByDivisionId(divisionId);
        System.out.println("Services trouvés: " + services.size());
        return ResponseEntity.ok(services);
    }
}
