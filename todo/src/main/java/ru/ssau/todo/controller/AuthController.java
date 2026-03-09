package ru.ssau.todo.controller;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.ssau.todo.dto.TokenResponse;
import ru.ssau.todo.dto.UserRequest;
import ru.ssau.todo.repository.UserRepository;
import ru.ssau.todo.security.CustomUserDetails;
import ru.ssau.todo.security.TokenService;
import ru.ssau.todo.service.CustomUserDetailsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, UserRepository userRepository, CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody @Valid UserRequest userRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRequest.getUsername(),
                        userRequest.getPassword()
                )
        );
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        long now = System.currentTimeMillis() / 1000;
        Map<String,Object> accessPayload = new HashMap<>();
        accessPayload.put("userId", customUserDetails.getId());
        accessPayload.put("roles", customUserDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList());
        accessPayload.put("iat", now);
        accessPayload.put("exp", now + 900);
        String accessToken = tokenService.generateToken(accessPayload);
        Map<String,Object> refreshPayload = new HashMap<>();
        refreshPayload.put("userId", customUserDetails.getId());
        refreshPayload.put("iat", now);
        refreshPayload.put("exp", now + 604800);
        String refreshToken = tokenService.generateToken(refreshPayload);
        return new TokenResponse(accessToken, refreshToken);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, Object> payload = tokenService.parseAndValidate(refreshToken);
        Long userId = ((Number) payload.get("userId")).longValue();
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
                userRepository.findById(userId).orElseThrow().getUsername());
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> accessPayload = new HashMap<>();
        accessPayload.put("userId", userDetails.getId());
        accessPayload.put("roles", userDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList());
        accessPayload.put("iat", now);
        accessPayload.put("exp", now + 900);
        String newAccessToken = tokenService.generateToken(accessPayload);
        return new TokenResponse(newAccessToken, refreshToken);
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Map<String, Object> response = new HashMap<>();
        response.put("username", customUserDetails.getUsername());
        response.put("roles", customUserDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority())
                .toList());
        return response;
    }
}