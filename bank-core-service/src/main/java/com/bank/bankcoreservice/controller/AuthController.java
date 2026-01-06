package com.bank.bankcoreservice.controller;

import com.bank.bankcoreservice.service.CustomUserDetailsService;
import com.bank.common.dto.LoginRequest;
import com.bank.common.dto.LoginResponse;
import com.bank.bankcoreservice.model.Account;
import com.bank.bankcoreservice.model.User;
import com.bank.bankcoreservice.repository.AccountRepository;
import com.bank.bankcoreservice.repository.UserRepository;
import com.bank.bankcoreservice.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // Dacă autentificarea trece, generăm token
        var user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        var jwtToken = jwtService.generateToken(new CustomUserDetailsService(userRepository).loadUserByUsername(user.getUsername()));

        LoginResponse response = new LoginResponse();
        response.setToken(jwtToken);
        return ResponseEntity.ok(response);
    }

    // Endpoint ajutător pentru a crea useri rapid (doar pentru test)
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody LoginRequest request) {


        if (request.getId() == null) {
            return ResponseEntity.badRequest().body("Eroare: Trebuie să introduci un ID manual!");
        }

        if (userRepository.existsById(request.getId())) {
            return ResponseEntity.badRequest().body("Eroare: ID-ul " + request.getId() + " este deja ocupat!");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Eroare: Username-ul există deja!");
        }

        User user = new User();
        user.setId(request.getId());

        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(user);

        Account account = new Account();
        account.setId(savedUser.getId());
        account.setUser(savedUser);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountNumber("RO" + System.currentTimeMillis());

        accountRepository.save(account);

        return ResponseEntity.ok("User registered successfully with ID: " + request.getId());
    }
}