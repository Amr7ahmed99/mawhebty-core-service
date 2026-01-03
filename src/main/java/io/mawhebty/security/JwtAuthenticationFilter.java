package io.mawhebty.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.mawhebty.enums.UserStatusEnum;
import io.mawhebty.models.User;
import io.mawhebty.services.UserService;
import io.mawhebty.services.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserService userService;
    private final CustomUserDetails userDetails;

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/internal-services/core/", // actually keycloakFilter will handle it
            "/api/v1/users/",
            "/api/auth/",
            "/swagger-ui/",
            "/v3/api-docs",
            "/actuator/health"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.debug("Processing request for path: {}", path);

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            log.debug("Skipping authentication for public path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // Skip if no Authorization header or not Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found for path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }


        try {

            String cleanToken = jwtService.cleanToken(authHeader);

            // Extract user email from JWT token
            String userEmail = jwtService.getUserEmailFromToken(cleanToken);

            if (userEmail == null) {
                log.warn("Invalid JWT token - no email claim for path: {}", path);
                sendErrorResponse(response, "Invalid token - no email claim");
                return;
            }

            // If we have email and no existing authentication
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.findByEmailFetchStatusAndRole(userEmail);

                if (user == null) {
                    log.warn("User not found for email: {} path: {}", userEmail, path);
                    sendErrorResponse(response, "User not found");
                    return;
                }

                // Validate token (both full and limited access)
                if (!jwtService.validateToken(cleanToken)) {
                    log.warn("Invalid JWT token for user: {} path: {}", userEmail, path);
                    sendErrorResponse(response, "Invalid token");
                    return;
                }

                // Verify token belongs to this user
                Long tokenUserId = jwtService.getUserIdFromToken(cleanToken);
                if (!user.getId().equals(tokenUserId)) {
                    log.warn("Token user ID mismatch for user: {} path: {}", userEmail, path);
                    sendErrorResponse(response, "Token user ID mismatch");
                    return;
                }

                // Check if user is active or has limited access
                boolean isActiveUser = user.getStatus() != null &&
                        UserStatusEnum.ACTIVE.getName().equals(user.getStatus().getName());
                boolean isFullAccessToken = jwtService.isFullAccessToken(cleanToken);

                // Validate token type matches user status
                if (isActiveUser && !isFullAccessToken) {
                    log.warn("Active user has limited access token: {} path: {}", userEmail, path);
                } else if (!isActiveUser && isFullAccessToken) {
                    log.warn("Inactive user has full access token: {} path: {}", userEmail, path);
                }

                userDetails.setUser(user);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Authenticated user: {} with roles: {} path: {}",
                        userEmail, userDetails.getAuthorities(), path);
            }

            // Continue with the filter chain
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("JWT Token expired for path: {}", path);
            sendErrorResponse(response, "Token has expired");

        } catch (Exception e) {
            log.error("Failed to process JWT token for path: {} - Error: {}", path, e.getMessage(), e);
            sendErrorResponse(response, e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}",
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), message, HttpStatus.UNAUTHORIZED.value()
        );

        response.getWriter().write(jsonResponse);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return isPublicPath(path);
    }
}