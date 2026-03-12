package ru.ssau.todo.controller;

import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.ssau.todo.dto.TokenResponse;
import ru.ssau.todo.dto.UserRequest;
import ru.ssau.todo.exception.TokenValidationException;
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
                new UsernamePasswordAuthenticationToken(userRequest.getUsername(), userRequest.getPassword())
        );
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return buildTokenResponse(customUserDetails);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody Map<String, String> request) throws TokenValidationException {
        String refreshToken = request.get("refreshToken");
        Map<String, Object> payload = tokenService.parseAndValidate(refreshToken);
        Long userId = ((Number) payload.get("userId")).longValue();
        CustomUserDetails customUserDetails = (CustomUserDetails) customUserDetailsService.loadUserByUsername(
                userRepository.findById(userId).orElseThrow().getUsername());
        String newAccessToken = tokenService.generateToken(buildAccessPayload(customUserDetails));
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

    private TokenResponse buildTokenResponse(CustomUserDetails customUserDetails) {
        String accessToken = tokenService.generateToken(buildAccessPayload(customUserDetails));
        String refreshToken = tokenService.generateToken(buildRefreshPayload(customUserDetails));
        return new TokenResponse(accessToken, refreshToken);
    }

    private Map<String, Object> buildAccessPayload(CustomUserDetails customUserDetails) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", customUserDetails.getId());
        payload.put("username", customUserDetails.getUsername());
        payload.put("roles",  customUserDetails.getAuthorities()
                .stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .toList());
        payload.put("iat", now);
        payload.put("exp", now + 900);
        return payload;
    }

    private Map<String, Object> buildRefreshPayload(CustomUserDetails customUserDetails) {
        long now = System.currentTimeMillis() / 1000;
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId",  customUserDetails.getId());
        payload.put("iat", now);
        payload.put("exp", now + 604800);
        return payload;
    }
}