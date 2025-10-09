package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.RegisterRequest;
import com.trimminflow.demo.dto.RegisterResponse;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.entity.UserRole;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final BarbershopRepository barbershopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(BarbershopRepository barbershopRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.barbershopRepository = barbershopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse registerBarbershopOwner(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create barbershop
        Barbershop barbershop = new Barbershop();
        barbershop.setName(request.getBarbershopName());
        barbershop.setEmail(request.getEmail());
        barbershop.setPhone(request.getPhone());
        barbershop.setAddress(request.getAddress());
        Barbershop savedBarbershop = barbershopRepository.save(barbershop);

        // Create owner user
        User owner = new User();
        owner.setEmail(request.getEmail());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setPassword(passwordEncoder.encode(request.getPassword()));
        owner.setBarbershop(savedBarbershop);
        owner.setRole(UserRole.ADMIN);
        User savedUser = userRepository.save(owner);

        return new RegisterResponse(
            savedUser.getId(),
            savedBarbershop.getId(),
            savedUser.getEmail(),
            "Barbershop registered successfully"
        );
    }
}