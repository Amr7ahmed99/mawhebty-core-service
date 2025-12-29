package io.mawhebty.security;

import io.mawhebty.handlers.OAuth2FailureHandler;
import io.mawhebty.handlers.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtLimitedAccessFilter jwtLimitedAccessFilter;
    private final KeycloakAuthFilter keycloakAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final AuthenticationManagerEntryPoint authenticationManagerEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Security Filter Chain with proper filter ordering
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF disabled for stateless API
                .csrf(AbstractHttpConfigurer::disable)

                // Session management - stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


                // Filter order: Keycloak → JwtAuthentication → JwtLimitedAccess
                .addFilterBefore(keycloakAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtLimitedAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // OAuth2 configuration (if needed)
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )

                // Authorization rules - CLEAR AND ORGANIZED
                .authorizeHttpRequests(auth -> auth
                        // ========== PUBLIC ENDPOINTS (NO AUTH REQUIRED) ==========
                        .requestMatchers(
                                // Health checks
                                "/health",
                                "/actuator/health",
                                "/actuator/info",

                                // API documentation
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",

                                // Authentication endpoints
                                "/api/v1/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/login",
                                "/api/v1/users/**",

                                // Public APIs
                                "/api/v1/public/**"
                        ).permitAll()

                        // ========== PUBLIC READ-ONLY ENDPOINTS ==========
                        .requestMatchers(
                                "/api/v1/categories/**",
                                "/api/v1/sub-categories/**",
                                "/api/v1/talent/**"
                        ).permitAll() //  .hasAnyRole("USER", "ADMIN")

                        // ========== INTERNAL SERVICES (Keycloak protected) ==========
                        .requestMatchers("/api/v1/internal-services/core/**")
                        .authenticated() // Keycloak filter will handle this

                        // ========== MAWHEBTY PLATFORM (JWT protected) ==========
                        .requestMatchers("/api/v1/mawhebty-platform/**")
                        .authenticated() // Custom JWT filter will handle this

                        // ========== ADMIN ENDPOINTS ==========
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        // ========== USER MANAGEMENT ENDPOINTS ==========
//                        .requestMatchers(
//                                "/api/v1/users/profile/**",
//                                "/api/v1/users/me/**"
//                        ).authenticated()

                        // User registration/management (public with rate limiting)
                        .requestMatchers(
                                "/api/v1/users/register",
                                "/api/v1/users/verify/**",
                                "/api/v1/users/reset-password/**"
                        ).permitAll()

                        // ========== FALLBACK - ALL OTHER API ENDPOINTS ==========
                        .requestMatchers("/api/**").authenticated()

                        // ========== DEFAULT - ANY OTHER REQUEST ==========
                        .anyRequest().permitAll()
                ).exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationManagerEntryPoint)
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Access Denied\"}");
                    })
                );

        return http.build();
    }
}