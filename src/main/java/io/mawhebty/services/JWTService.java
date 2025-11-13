package io.mawhebty.services;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import javax.crypto.SecretKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.mawhebty.dtos.responses.TokenResponse;
import io.mawhebty.enums.PermissionEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.mawhebty.enums.UserRoleEnum;
import io.mawhebty.enums.UserStatusEnum;
import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.models.User;
import io.mawhebty.models.UserRole;
import io.mawhebty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWTService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshExpiration}")
    private Long refreshTokenExpiration;

    private final UserRepository userRepository;

    public String generateToken(Long userId, UserRole role, String tokenType) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role.getName());
        claims.put("tokenType", tokenType);
        claims.put("userStatus", user.getStatus().getName());
        claims.put("permissions", "LIMITED_ACCESS".equals(tokenType)? getLimitedPermissions(role): getFullPermissions(role));

        return buildToken(claims, userId.toString(), accessTokenExpiration);
    }

    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "REFRESH");
        claims.put("userId", userId);

        return buildToken(claims, userId.toString(), refreshTokenExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, Long expiration) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + expiration))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    // ========== TOKEN VALIDATION METHODS ==========

    public boolean isLimitedToken(String token) {
        try {
            String tokenType = getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
            return "LIMITED_ACCESS".equals(tokenType);
        } catch (Exception e) {
            log.warn("Failed to check if token is limited: {}", e.getMessage());
            return false;
        }
    }

    public boolean isFullAccessToken(String token) {
        try {
            String tokenType = getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
            return "FULL_ACCESS".equals(tokenType);
        } catch (Exception e) {
            log.warn("Failed to check if token is full access: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getClaimFromToken(token, claims -> claims.get("tokenType", String.class));
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            log.warn("Failed to check if token is refresh token: {}", e.getMessage());
            return false;
        }
    }

    public UserStatusEnum getUserStatusFromToken(String token) {
        try {
            String status = getClaimFromToken(token, claims -> claims.get("userStatus", String.class));
            return UserStatusEnum.valueOf(status);
        } catch (Exception e) {
            log.warn("Failed to get user status from token: {}", e.getMessage());
            return UserStatusEnum.DRAFT; // Default fallback
        }
    }

    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    public String getUserRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    public List<String> getPermissionsFromToken(String token) {
        try {
            List<?> rawList = getClaimFromToken(token, claims -> claims.get("permissions", List.class));
            if (rawList == null) {
                return new ArrayList<>();
            }
            List<String> permissions = new ArrayList<>(rawList.size());
            for (Object item : rawList) {
                if (item != null) {
                    permissions.add(item.toString());
                }
            }
            return permissions;
        } catch (Exception e) {
            log.warn("Failed to get permissions from token: {}", e.getMessage());
            return new ArrayList<>();
        }
    }


    // ========== CORE JWT METHODS ==========

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.warn("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // ========== PERMISSION METHODS ==========
    public List<Integer> getLimitedPermissions(UserRole role) {
        //TODO: read is_limited permissions dependent on role from DB
        return Arrays.asList(
                PermissionEnum.READ_PROFILE.getId(),
                PermissionEnum.READ_FEEDS.getId(),
                PermissionEnum.READ_POSTS.getId(),
                PermissionEnum.SEARCH.getId(),
                PermissionEnum.VIEW_TALENTS.getId(),
                PermissionEnum.VIEW_RESEARCHERS.getId()
        );
    }

    public List<Integer> getFullPermissions(UserRole role) {
        List<Integer> permissions = new ArrayList<>(getLimitedPermissions(role));

        // Add write permissions based on role
        permissions.add(PermissionEnum.LIKE.getId());
        permissions.add(PermissionEnum.COMMENT.getId());
        permissions.add(PermissionEnum.FOLLOW.getId());
        permissions.add(PermissionEnum.SAVE_POSTS.getId());

        //TODO: read is_limited permissions dependent on role from DB
        if (UserRoleEnum.TALENT.getName().equals(role.getName())) {
            permissions.add(PermissionEnum.MANAGE_TALENT_PROFILE.getId());
            permissions.add(PermissionEnum.RECEIVE_CONTRACTS.getId());
            permissions.add(PermissionEnum.UPLOAD_CONTENT.getId());
        } else if (UserRoleEnum.RESEARCHER.getName().equals(role.getName())) {
            permissions.add(PermissionEnum.MANAGE_RESEARCHER_PROFILE.getId());
            permissions.add(PermissionEnum.SEND_CONTRACTS.getId());
            permissions.add(PermissionEnum.BOOST_LISTINGS.getId());
        }
        //  else if ("ADMIN".equals(role.getName())) {
        //     permissions.add("MANAGE_USERS");
        //     permissions.add("MODERATE_CONTENT");
        //     permissions.add("VIEW_ANALYTICS");
        // }
        return permissions;
    }

    public boolean hasPermission(String token, String permission) {
        try {
            List<String> permissions = getPermissionsFromToken(token);
            return permissions.contains(permission);
        } catch (Exception e) {
            log.warn("Failed to check permission: {}", e.getMessage());
            return false;
        }
    }

    // ========== UTILITY METHODS ==========

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Long getRemainingTokenTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.warn("Failed to get remaining token time: {}", e.getMessage());
            return 0L;
        }
    }

    public TokenResponse determineSuitableTokenResponse(User user){

        switch (user.getStatus().getName()) {
            // if user has profile and status is active return full access and refresh token
            case "ACTIVE":
                String token = generateToken(user.getId(), user.getRole(), "FULL_ACCESS");
                String refreshToken = generateRefreshToken(user.getId());

                return TokenResponse.builder()
                        .accessToken(token)
                        .refreshToken(refreshToken)
                        .tokenType("FULL_ACCESS")
                        .expiresIn(getRemainingTokenTime(token))
                        .permissions(getFullPermissions(user.getRole()))
                        .userStatus(user.getStatus().getName())
                        .userRole(user.getRole().getName())
                        .build();

            case "PENDING_MODERATION":

                // if user has profile and status is PENDING_MODERATION return limited access token
                token = generateToken(user.getId(), user.getRole(), "LIMITED_ACCESS");
                refreshToken = generateRefreshToken(user.getId());

                return TokenResponse.builder()
                        .accessToken(token)
                        .refreshToken(refreshToken)
                        .tokenType("LIMITED_ACCESS")
                        .expiresIn(getRemainingTokenTime(token))
                        .permissions(getLimitedPermissions(user.getRole()))
                        .userStatus(user.getStatus().getName())
                        .userRole(user.getRole().getName())
                        .build();
            default:
                return null;
        }
    }


    /**
     * Check if token is from Keycloak
     */
    public boolean isKeycloakToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = new ObjectMapper().readValue(payload, Map.class);

            // Check if 'iss' claim contains our Keycloak domain
            String issuer = (String) claims.get("iss");
            return issuer != null &&
                    (issuer.contains("auth.mawhebty.com") ||
                            issuer.contains("keycloak") ||
                            issuer.equals("https://auth.mawhebty.com/realms/mawhebty") ||
                            issuer.equals("https://auth.mawhebty.com/realms/Mawhebty"));
        } catch (Exception e) {
            log.error("Token is not Keycloak: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username from Keycloak token
     */
    public String getUsernameFromKeycloakToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

            Map<String, Object> claims = new ObjectMapper()
                    .readValue(payload, Map.class);

            return (String) claims.get("preferred_username");
        } catch (Exception e) {
            log.error("Failed to extract username from Keycloak token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Debug method to see all token claims
     */
    public void debugToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

            Map<String, Object> claims = new ObjectMapper()
                    .readValue(payload, Map.class);

            log.info("=== TOKEN DEBUG INFO ===");
            log.info("Issuer (iss): {}", claims.get("iss"));
            log.info("Subject (sub): {}", claims.get("sub"));
            log.info("Username (preferred_username): {}", claims.get("preferred_username"));
            log.info("Realm Access: {}", claims.get("realm_access"));
            log.info("Resource Access: {}", claims.get("resource_access"));
            log.info("All claims: {}", claims);

        } catch (Exception e) {
            log.error("Token debug failed: {}", e.getMessage());
        }
    }

    /**
     * Extract roles from Keycloak token
     */
    public List<String> getRolesFromKeycloakToken(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

            Map<String, Object> claims = new ObjectMapper()
                    .readValue(payload, Map.class);

            // Extract from realm_access (global roles)
            Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
            if (realmAccess != null) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                if (roles != null) return roles;
            }

            return List.of();
        } catch (Exception e) {
            log.error("Failed to extract roles from Keycloak token: {}", e.getMessage());
            return List.of();
        }
    }
}
