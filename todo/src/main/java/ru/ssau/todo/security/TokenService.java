package ru.ssau.todo.security;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
public class TokenService {
    private final ObjectMapper mapper = new ObjectMapper();

    private String getSecret() {
        return System.getenv("JWT_SECRET");
    }

    public String generateToken(Map<String, Object> payload) {
        try  {
            String json = mapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(json.getBytes());
            Mac mac = Mac.getInstance("HmacSHA256");
            //SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(getSecret().getBytes(), "HmacSHA256");
            mac.init(key);
            byte[] signatureByte = mac.doFinal(encodedPayload.getBytes());
            String encodedSignature = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(signatureByte);
            return encodedPayload + "." +  encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Object> parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 2) {
            throw new RuntimeException("Invalid token format");
        }
        String payloadPart = parts[0];
        String signaturePart = parts[1];
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(getSecret().getBytes(), "HmacSHA256");
            mac.init(key);
            byte[] signatureBytes = mac.doFinal(payloadPart.getBytes());
            String expectedSignature = Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
            if (!expectedSignature.equals(signaturePart)) {
                throw new RuntimeException("Invalid signature");
            }
            byte[] decoded = Base64.getUrlDecoder().decode(payloadPart);
            Map<String, Object> payload = mapper.readValue(new String(decoded), Map.class);
            long exp = ((Number) payload.get("exp")).longValue();
            if (exp < (System.currentTimeMillis() / 1000)) {
                throw new RuntimeException("Token expired");
            }
            return payload;
        } catch (Exception e) {
            throw new RuntimeException("Token validation failed", e);
        }
    }
}
