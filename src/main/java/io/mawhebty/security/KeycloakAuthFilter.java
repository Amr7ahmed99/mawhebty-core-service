package io.mawhebty.security;

import io.mawhebty.services.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakAuthFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Apply ONLY to internal admin endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/internal-services/core/")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendKeycloakError(response, "Missing or invalid Authorization header");
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // MUST be a Keycloak token for internal endpoints
            if (!jwtService.isKeycloakToken(jwt)) {
                sendKeycloakError(response, "Keycloak token required for internal endpoints");
                return;
            }

            // Validate token structure and extract claims
            String username = jwtService.getUsernameFromKeycloakToken(jwt);
            List<String> roles = jwtService.getRolesFromKeycloakToken(jwt);

            if (username == null || roles == null) {
                sendKeycloakError(response, "Invalid Keycloak token");
                return;
            }

            // Check if user has DASHBOARD_ADMIN role
            if (!roles.contains("DASHBOARD_ADMIN")) {
                sendKeycloakError(response, "DASHBOARD_ADMIN role required");
                return;
            }

            // Create authentication
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("")
                    .authorities(authorities)
                    .build();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("Authenticated Keycloak admin: {} for path: {}", username, path);

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Keycloak authentication failed: {}", e.getMessage());
            sendKeycloakError(response, "Keycloak authentication failed");
        }
    }

//    Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        String path = request.getRequestURI();
//        if (!path.startsWith("/api/v1/internal-services/core/")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        final String authHeader = request.getHeader("Authorization");
//        final String jwt;
//
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            sendKeycloakError(response, "Missing or invalid Authorization header");
//            return;
//        }
//
//        jwt = authHeader.substring(7);
//
//        try {
//            // Debug the token first
//            jwtService.debugToken(jwt);
//
//            // Check if it's Keycloak token
//            if (!jwtService.isKeycloakToken(jwt)) {
//                sendKeycloakError(response, "Keycloak token required for internal endpoints");
//                return;
//            }
//
//            // Validate token structure and extract claims
//            String username = jwtService.getUsernameFromKeycloakToken(jwt);
//            List<String> roles = jwtService.getRolesFromKeycloakToken(jwt);
//
//            if (username == null || roles == null) {
//                sendKeycloakError(response, "Invalid Keycloak token");
//                return;
//            }
//
//            // Check if user has ADMIN role
//            if (!roles.contains("ADMIN")) {
//                sendKeycloakError(response, "ADMIN role required");
//                return;
//            }
//
//            // Create authentication
//            List<SimpleGrantedAuthority> authorities = roles.stream()
//                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                    .collect(Collectors.toList());
//
//            UserDetails userDetails = User.builder()
//                    .username(username)
//                    .password("")
//                    .authorities(authorities)
//                    .build();
//
//            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                    userDetails,
//                    null,
//                    userDetails.getAuthorities()
//            );
//            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//            SecurityContextHolder.getContext().setAuthentication(authToken);
//            log.info("Authenticated Keycloak admin: {} for path: {}", username, path);
//
//            filterChain.doFilter(request, response);
//        } catch (Exception e) {
//            log.error("Keycloak authentication failed: {}", e.getMessage());
//            sendKeycloakError(response, "Keycloak authentication failed");
//        }
//    }

    private void sendKeycloakError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format("{\"error\": \"KEYCLOAK_AUTH_ERROR\", \"message\": \"%s\"}", message)
        );
    }
}