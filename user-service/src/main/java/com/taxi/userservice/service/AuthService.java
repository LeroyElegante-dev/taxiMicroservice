package com.taxi.userservice.service;

import com.taxi.common.dto.AuthLoginRequest;
import com.taxi.common.dto.AuthRegisterRequest;
import com.taxi.common.dto.JwtAuthResponse;
import com.taxi.common.exception.DuplicateResourceException;
import com.taxi.security.JwtProperties;
import com.taxi.security.JwtTokenService;
import com.taxi.userservice.entity.UserAccountEntity;
import com.taxi.userservice.repository.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService,
            JwtProperties jwtProperties
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public JwtAuthResponse register(AuthRegisterRequest request) {
        String username = request.username().trim().toLowerCase();
        if (userAccountRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Пользователь с таким логином уже зарегистрирован");
        }
        UserAccountEntity entity = new UserAccountEntity();
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(request.password()));
        entity.setRole("USER");
        userAccountRepository.save(entity);
        return token(username, entity.getRole());
    }

    @Transactional(readOnly = true)
    public JwtAuthResponse login(AuthLoginRequest request) {
        String username = request.username().trim().toLowerCase();
        UserAccountEntity entity = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));
        if (!passwordEncoder.matches(request.password(), entity.getPasswordHash())) {
            throw new BadCredentialsException("Неверный логин или пароль");
        }
        return token(entity.getUsername(), entity.getRole());
    }

    private JwtAuthResponse token(String username, String role) {
        String jwt = jwtTokenService.generateToken(username, role);
        return JwtAuthResponse.of(jwt, jwtProperties.getExpirationSeconds());
    }
}
