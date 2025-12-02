package com.trimminflow.demo.service;

import com.trimminflow.demo.dto.LoginRequest;
import com.trimminflow.demo.dto.LoginResponse;
import com.trimminflow.demo.entity.Barbershop;
import com.trimminflow.demo.entity.User;
import com.trimminflow.demo.entity.UserRole;
import com.trimminflow.demo.repository.UserRepository;
import com.trimminflow.demo.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository userRepo;
    @Mock
    PasswordEncoder encoder;
    @Mock
    JwtUtil jwt;
    @InjectMocks
    AuthService authService;

    // ./gradlew test --tests com.trimminflow.demo.service.AuthServiceTest

    @Test
    void testLoginSuccess() {
        // setup data
        String mail = "test@test.com";
        String pass = "123456";
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(id);
        user.setEmail(mail);
        user.setPassword("encoded");
        user.setRole(UserRole.CUSTOMER);
        user.setBarbershop(new Barbershop());

        LoginRequest req = new LoginRequest();
        req.setEmail(mail);
        req.setPassword(pass);

        // mock calls
        when(userRepo.findByEmail(mail)).thenReturn(Optional.of(user));
        when(encoder.matches(pass, "encoded")).thenReturn(true);
        when(jwt.generateToken(mail, id, "CUSTOMER")).thenReturn("token123");

        // run test
        LoginResponse res = authService.login(req);

        // check results
        assertEquals("token123", res.getAccessToken());
        assertEquals(mail, res.getEmail());
    }

    @Test
    void login_throwsRuntimeException_whenTheEmailOrPasswordDoesNotExist() {
        // setup
        String mail = "test@test.com";
        User user = new User();
        user.setEmail(mail);
        user.setPassword("encoded");

        LoginRequest req = new LoginRequest();
        req.setEmail(mail);
        req.setPassword("wrong");

        // mock
        when(userRepo.findByEmail(mail)).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "encoded")).thenReturn(false);

        // verify error
        assertThrows(RuntimeException.class, () -> authService.login(req));
    }
}
