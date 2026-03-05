package com.chat.auth.authservice.service;

import com.chat.auth.authservice.dto.AuthDto;
import com.chat.auth.authservice.model.User;
import com.chat.auth.authservice.repository.UserRepository;
import com.chat.auth.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username '" + request.getUsername() + "' is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .displayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername())
            .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getUsername());

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return buildAuthResponse(token, user);
    }

    @Transactional
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Update status to online
        user.setStatus(User.UserStatus.ONLINE);
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        log.info("User logged in: {}", user.getUsername());
        return buildAuthResponse(token, user);
    }

    public AuthDto.UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return toUserDto(user);
    }

    private AuthDto.AuthResponse buildAuthResponse(String token, User user) {
        return AuthDto.AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .user(toUserDto(user))
            .build();
    }

    private AuthDto.UserDto toUserDto(User user) {
        return AuthDto.UserDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .status(user.getStatus() != null ? user.getStatus().name() : "ONLINE")
            .build();
    }
}
