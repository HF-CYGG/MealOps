package com.cjc.mealops.util;

import com.cjc.mealops.common.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class JwtUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final byte[] secret;
    private final Duration ttl;

    public JwtUtils(String secret, Duration ttl) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttl = ttl;
    }

    public String generate(Long userId, String role) {
        long expiresAt = System.currentTimeMillis() + ttl.toMillis();
        try {
            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("uid", userId);
            claims.put("role", role);
            claims.put("exp", expiresAt);
            String payload = encodeJson(claims);
            String unsigned = header + "." + payload;
            return unsigned + "." + sign(unsigned);
        } catch (Exception ex) {
            throw new BusinessException("Token 生成失败");
        }
    }

    public JwtPayload parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException("Token 格式错误");
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!constantEquals(sign(unsigned), parts[2])) {
                throw new BusinessException("Token 签名无效");
            }
            JsonNode json = MAPPER.readTree(Base64.getUrlDecoder().decode(parts[1]));
            long expiresAt = json.get("exp").asLong();
            if (expiresAt < System.currentTimeMillis()) {
                throw new BusinessException("登录已过期");
            }
            return new JwtPayload(json.get("uid").asLong(), json.get("role").asText(), expiresAt);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("Token 解析失败");
        }
    }

    private String encodeJson(Map<String, ?> value) throws Exception {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(MAPPER.writeValueAsBytes(value));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public record JwtPayload(Long userId, String role, Long expiresAt) {
    }
}
