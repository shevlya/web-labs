package ru.ssau.todo.security;

import org.springframework.stereotype.Service;
import ru.ssau.todo.exception.TokenValidationException;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
public class TokenService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final String secret = System.getenv("JWT_SECRET");

    public TokenService() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET  не задан");
        }
    }

    public String generateToken(Map<String, Object> payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            String encodedPayload = encode(json);
            String encodedSignature = sign(encodedPayload);
            return encodedPayload + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации токена", e);
        }
    }

    public Map<String, Object> parseAndValidate(String token) throws TokenValidationException {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                throw new TokenValidationException("Неверный формат токена");
            }
            String payloadPart = parts[0];
            String signaturePart = parts[1];
            if (!sign(payloadPart).equals(signaturePart)) {
                throw new TokenValidationException("Неверная подпись токена");
            }
            Map<String, Object> payload = decode(payloadPart);
            validateExpiration(payload);
            return payload;
        } catch (TokenValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new TokenValidationException("Ошибка валидации токена");
        }
    }

    private String encode(String data) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(data.getBytes());
    }

    private Map<String, Object> decode(String encoded) throws Exception {
        byte[] decoded = Base64.getUrlDecoder().decode(encoded);
        return mapper.readValue(decoded, Map.class);
    }

    private void validateExpiration(Map<String, Object> payload) throws TokenValidationException {
        long exp = ((Number) payload.get("exp")).longValue();
        long now = System.currentTimeMillis() / 1000;
        if (exp < now) {
            throw new TokenValidationException("Токен истёк");
        }
    }

    private String sign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(key);
        byte[] signatureByte = mac.doFinal(data.getBytes());
        String encodedSignature = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(signatureByte);
        return encodedSignature;
    }
}
