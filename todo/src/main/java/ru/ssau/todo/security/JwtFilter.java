package ru.ssau.todo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final ObjectMapper mapper = new ObjectMapper();

    private String getSecret() {
        return System.getenv("JWT_SECRET");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/auth/login") || path.equals("/auth/refresh") || path.equals("/users/register");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String token = header.substring(7);
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String payloadPart = parts[0];
        String signaturePart = parts[1];
        try {
            if (!isSignatureValid(payloadPart, signaturePart)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            byte[] decoded = Base64.getUrlDecoder().decode(payloadPart);
            Map<String, Object> payload = mapper.readValue(new String(decoded), Map.class);
            long exp = ((Number) payload.get("exp")).longValue();
            long now = System.currentTimeMillis() / 1000;
            if (exp < now) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            String username = payload.get("username") != null ? (String) payload.get("username") : "unknown";
            Long userId = ((Number) payload.get("userId")).longValue();
            List<SimpleGrantedAuthority> authorities = buildAuthorities((List<String>) payload.get("roles"));
            CustomUserDetails customUserDetails = new CustomUserDetails(userId, username, "", authorities);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean isSignatureValid(String payloadPart, String signaturePart) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(getSecret().getBytes(), "HmacSHA256");
        mac.init(key);
        byte[] signatureByte = mac.doFinal(payloadPart.getBytes());
        String encodedSignature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(signatureByte);
        return encodedSignature.equals(signaturePart);
    }

    private List<SimpleGrantedAuthority> buildAuthorities(List<String> roles) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (roles != null) {
            for (String role : roles) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
        }
        return authorities;
    }
}
