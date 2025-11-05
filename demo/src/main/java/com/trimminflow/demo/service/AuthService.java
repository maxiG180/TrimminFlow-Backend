package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.LoginRequest;
import com.trimminflow.demo.dto.LoginResponse;
import com.trimminflow.demo.dto.RegisterRequest;
import com.trimminflow.demo.dto.RegisterResponse;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.entity.UserRole;
import com.trimminflow.demo.repository.BarbershopRepository;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final BarbershopRepository barbershopRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(BarbershopRepository barbershopRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil) {
        this.barbershopRepository = barbershopRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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

    /**
     * Login a user and return JWT token
     *
     * @param request LoginRequest with email and password
     * @return LoginResponse with JWT access token and user information
     * @throws RuntimeException if credentials are invalid
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Update last login time
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String accessToken = jwtUtil.generateToken(
            user.getEmail(),
            user.getId(),
            user.getRole().name()
        );

        // Return response with token and user info
        return new LoginResponse(
            accessToken,
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name(),
            user.getBarbershop().getId(),
            "Login successful"
        );
    }
}