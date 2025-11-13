// package io.mawhebty.security;

// import java.io.IOException;

// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import io.jsonwebtoken.ExpiredJwtException;
// import io.mawhebty.enums.UserStatusEnum;
// import io.mawhebty.services.JWTService;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.AllArgsConstructor;

// @Component
// @AllArgsConstructor
// public class JwtLimitedAccessFilter extends OncePerRequestFilter {
    
//     private JWTService jwtService;
//     @Override
//     protected void doFilterInternal(HttpServletRequest request, 
//                                   HttpServletResponse response, 
//                                   FilterChain filterChain) throws ServletException, IOException {
        
//         // String token = getTokenFromRequest(request);
//         final String requestTokenHeader = request.getHeader("Authorization");
//         String email;
//         String jwtToken = null;
//         if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
//             jwtToken = requestTokenHeader.substring(7);
//             String extractedEmail = null;
//             try {
//                 extractedEmail = jwtUtil.extractUserEmail(jwtToken);
//             } catch (IllegalArgumentException e) {
//                 logger.error("Unable to get JWT Token");
//             } catch (ExpiredJwtException e) {
//                 logger.warn("JWT Token expired for path {}", path);

//                 // For non-refresh requests, stop the filter chain return 401
//                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                 response.setContentType("application/json");
//                 response.getWriter().write("{\"error\": \"Token has expired\"}");
//                 return; 
//             }
//             email= extractedEmail;
//         }else{
//             email= null;
//         }
        
//         if (requestTokenHeader != null && jwtService.isLimitedToken(requestTokenHeader)) {
//             UserStatusEnum userStatus = jwtService.getUserStatusFromToken(requestTokenHeader);
            
//             if (userStatus == UserStatusEnum.PENDING_MODERATION) {
//                 // Allow only specific GET endpoints
//                 if (isAllowedEndpoint(request) && request.getMethod().equals("GET")) {
//                     filterChain.doFilter(request, response);
//                     return;
//                 } else {
//                     response.sendError(HttpStatus.FORBIDDEN.value(), 
//                         "Account under moderation review. Please wait for approval.");
//                     return;
//                 }
//             }
//         }
        
//         filterChain.doFilter(request, response);
//     }
    
//     private boolean isAllowedEndpoint(HttpServletRequest request) {
//         String path = request.getRequestURI();
//         return path.startsWith("/api/v1/feeds") || 
//                path.startsWith("/api/v1/posts") || 
//                path.startsWith("/api/v1/search") ||
//                path.startsWith("/api/v1/talents") ||
//                path.startsWith("/api/v1/researchers") ||
//                path.equals("/api/v1/profile") ||
//                path.startsWith("/api/v1/categories");
//     }
// }

package io.mawhebty.security;

import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.mawhebty.enums.UserStatusEnum;
import io.mawhebty.services.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLimitedAccessFilter extends OncePerRequestFilter {
    
    private final JWTService jwtService;
    
    // List of public endpoints that don't require token checking
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/api/v1/auth/register",
        "/api/v1/auth/login", 
        "/api/v1/auth/verify-otp",
        "/api/v1/auth/refresh-token",
        "/api/v1/public/"
    );
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip filter for public endpoints
        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract token from Authorization header
        String token = extractTokenFromRequest(request);
        
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // Check if it's a limited token
            if (jwtService.isLimitedToken(token)) {
                UserStatusEnum userStatus = jwtService.getUserStatusFromToken(token);
                
                if (userStatus == UserStatusEnum.PENDING_MODERATION) {
                    // Allow only specific GET endpoints for pending moderation users
                    if (isAllowedEndpoint(path) && "GET".equalsIgnoreCase(method)) {
                        filterChain.doFilter(request, response);
                    } else {
                        sendErrorResponse(response, HttpStatus.FORBIDDEN.value(),
                            "Account under moderation review. Please wait for approval.");
                    }
                    return;
                }
            }
            
            // If not limited token or user is ACTIVE, continue
            filterChain.doFilter(request, response);
            
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token expired for path: {}", path);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), 
                "Token has expired");
        } catch (MalformedJwtException e) {
            log.warn("Malformed JWT Token for path: {}", path);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), 
                "Invalid token format");
        } catch (SignatureException e) {
            log.warn("Invalid JWT signature for path: {}", path);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), 
                "Invalid token signature");
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT Token for path: {}", path);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), 
                "Unsupported token type");
        } catch (Exception e) {
            log.error("JWT processing error for path: {}", path, e);
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED.value(), 
                "Token validation failed");
        }
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    private boolean isPublicEndpoint(String path) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }
    
    private boolean isAllowedEndpoint(String path) {
        return path.startsWith("/api/v1/feeds") || 
               path.startsWith("/api/v1/posts") || 
               path.startsWith("/api/v1/search") ||
               path.startsWith("/api/v1/talents") ||
               path.startsWith("/api/v1/researchers") ||
               path.equals("/api/v1/profile") ||
               path.startsWith("/api/v1/categories");
    }
    
    private void sendErrorResponse(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            String.format("{\"error\": \"%s\", \"message\": \"%s\", \"status\": %d}", 
                "AUTHORIZATION_ERROR", message, status)
        );
    }
    
    // Skip filter for OPTIONS requests (CORS preflight)
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}