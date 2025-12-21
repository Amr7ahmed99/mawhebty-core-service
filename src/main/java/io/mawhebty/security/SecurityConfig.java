//package io.mawhebty.security;
//
//import java.util.Arrays;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import lombok.RequiredArgsConstructor;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtLimitedAccessFilter jwtLimitedAccessFilter;
//    private final JwtAuthenticationFilter jwtAuthFilter;
//
//
//    @Value("${app.frontend.url:http://localhost:3000}")
//    private String frontendUrl;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) 
//            throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auths -> auths
//                // Public endpoints - no authentication required
//                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
////                .requestMatchers("api/v1/internal-services/core/**").hasRole("admin")
//                .requestMatchers("/api/v1/auth/**").permitAll() // All auth endpoints
//                    .requestMatchers("/api/v1/users/**").permitAll() // All auth endpoints
//                    .requestMatchers("/api/v1/public/**").permitAll() // Public APIs
//                // .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger
//                .requestMatchers("/actuator/health", "/error").permitAll() // Health check
//
//                .requestMatchers("/health-check").permitAll() // Health check
//
//                .requestMatchers("api/v1/internal-services/core/**").permitAll()
//                .requestMatchers("api/v1/categories/**").permitAll()
//                .requestMatchers("api/v1/sub-categories/**").permitAll()
//                .requestMatchers("api/v1/talent/**").permitAll()
//
//                    // Role-based access for specific endpoints
//                // .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//                // .requestMatchers("/api/v1/moderator/**").hasRole("MODERATOR")
//                
//                // All other endpoints require authentication
//                .anyRequest().authenticated()
//            )
////            .httpBasic(Customizer.withDefaults())
//            .sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            );
//
//        // Add our custom filter before the standard authentication filter
//        http.addFilterBefore(jwtLimitedAccessFilter, UsernamePasswordAuthenticationFilter.class);
//        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        
//        // Allow specific origins
//        configuration.setAllowedOriginPatterns(Arrays.asList(
//            frontendUrl, 
//            "http://localhost:3000", 
//            "https://localhost:3000",
//            "http://127.0.0.1:3000"
//        ));
//        
//        // Allow all HTTP methods
//        configuration.setAllowedMethods(Arrays.asList(
//            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
//        ));
//        
//        // Allow specific headers
//        configuration.setAllowedHeaders(Arrays.asList(
//            "Authorization",
//            "Content-Type",
//            "Accept",
//            "X-Requested-With",
//            "Cache-Control",
//            "Origin",
//            "Access-Control-Request-Method", 
//            "Access-Control-Request-Headers"
//        ));
//        
//        // Expose headers to frontend
//        configuration.setExposedHeaders(Arrays.asList(
//            "Authorization",
//            "Content-Disposition",
//            "X-Total-Count"
//        ));
//        
//        // Allow credentials (cookies, authorization headers)
//        configuration.setAllowCredentials(true);
//        
//        // Cache preflight response for 1 hour
//        configuration.setMaxAge(3600L);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}

package io.mawhebty.security;

import io.mawhebty.handlers.OAuth2FailureHandler;
import io.mawhebty.handlers.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtLimitedAccessFilter jwtLimitedAccessFilter;

    @Autowired
    private KeycloakAuthFilter keycloakAuthFilter;

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private OAuth2FailureHandler oAuth2FailureHandler;

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

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Security Filter Chain - Custom JWT for public, Keycloak for internal
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF disabled for stateless API
                .csrf(csrf -> csrf.disable())

                // Authorization rules
//                .authorizeHttpRequests(auth -> auth
//                                // Public endpoints - no authentication required
//                                .requestMatchers(
//                                        "/api/v1/auth/**",
//                                        "/api/v1/categories/**",
//                                        "/api/v1/sub-categories/**",
//                                        "/api/v1/talent/**",
//                                        "/health",
//                                        "/actuator/health",
//                                        "/api/v1/public/**"
//                                ).permitAll()
//
//                                .requestMatchers("/oauth2/**", "/login/oauth2/**", "/login").permitAll()
//
//                                // Internal admin endpoints - handled by Keycloak filter
//                                .requestMatchers("/api/v1/internal-services/core/**").authenticated()
//
//                                // Admin endpoints - can use either Custom JWT or Keycloak
//                                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
//
//                                // User endpoints - can use either Custom JWT or Keycloak
////                        .requestMatchers("/api/v1/user/**").hasAnyRole("USER", "ADMIN")
//                                .requestMatchers("/api/v1/user/**").authenticated()
//
//                                // Protected endpoints - can use either authentication
////                        .requestMatchers(
////                                "/api/v1/categories/**",
////                                "/api/v1/sub-categories/**",
////                                "/api/v1/talent/**"
////                        ).authenticated()
//
//                                // Any other API endpoint requires authentication
////                        .requestMatchers("/api/**").authenticated()
//
//                                // Allow everything else
//                                .anyRequest().authenticated()
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .successHandler(oAuth2SuccessHandler)
//                        .failureHandler(oAuth2FailureHandler)
//                )
                // Session management - stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Filter order: Keycloak → Limited Access → Custom JWT
                .addFilterBefore(keycloakAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtLimitedAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/health",
                                "/actuator/health",
                                "/api/v1/public/**",
                                "/api/v1/categories/**",
                                "/api/v1/sub-categories/**",
                                "/api/v1/talent/**",
                                "/health"
                        ).permitAll()

                        // Internal admin endpoints - handled by Keycloak filter
                        .requestMatchers("/api/v1/internal-services/core/**").authenticated()

                        // Admin endpoints - can use either Custom JWT or Keycloak
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // User endpoints - can use either Custom JWT or Keycloak
//                        .requestMatchers("/api/v1/users/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/v1/users/**").permitAll()

                        // Protected endpoints - can use either authentication
//                        .requestMatchers(
//                                "/api/v1/categories/**",
//                                "/api/v1/sub-categories/**",
//                                "/api/v1/talent/**"
//                        ).authenticated()

                        // Any other API endpoint requires authentication
                        .requestMatchers("/api/**").authenticated()
                );

        return http.build();
    }
}