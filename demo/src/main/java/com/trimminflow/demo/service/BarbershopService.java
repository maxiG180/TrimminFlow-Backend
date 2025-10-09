package com.trimminflow.demo.service;

import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.repository.BarbershopRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BarbershopService {

    private final BarbershopRepository barbershopRepository;

    public BarbershopService(BarbershopRepository barbershopRepository) {
        this.barbershopRepository = barbershopRepository;
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
}
