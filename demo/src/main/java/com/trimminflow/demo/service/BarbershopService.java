package com.trimminflow.demo.service;

import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.repository.BarbershopRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;
    private final CloudinaryService cloudinaryService;

    public BarbershopService(BarbershopRepository barbershopRepository, CloudinaryService cloudinaryService) {
        this.barbershopRepository = barbershopRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public List<Barbershop> getAllBarbershops() {
        return barbershopRepository.findAll();
    }

    public Optional<Barbershop> getBarbershopById(UUID id) {
        return barbershopRepository.findById(id);
    }

    public Barbershop createBarbershop(Barbershop barbershop) {
        return barbershopRepository.save(barbershop);
    }

    public Barbershop updateBarbershop(UUID id, Barbershop barbershopDetails) {
        Barbershop barbershop = barbershopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Barbershop not found with id: " + id));

        barbershop.setName(barbershopDetails.getName());
        barbershop.setAddress(barbershopDetails.getAddress());

        return barbershopRepository.save(barbershop);
    }

    public void deleteBarbershop(UUID id) {
        barbershopRepository.deleteById(id);
    }

    public String uploadLogo(UUID barbershopId, MultipartFile file) {
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        String logoUrl = cloudinaryService.uploadImage(file);
        barbershop.setLogoUrl(logoUrl);
        barbershopRepository.save(barbershop);

        return logoUrl;
    }

    public Barbershop updateReminderSettings(UUID barbershopId, Boolean reminderEmailsEnabled) {
        Barbershop barbershop = barbershopRepository.findById(barbershopId)
                .orElseThrow(() -> new RuntimeException("Barbershop not found"));

        barbershop.setReminderEmailsEnabled(reminderEmailsEnabled);
        return barbershopRepository.save(barbershop);
    }
}
