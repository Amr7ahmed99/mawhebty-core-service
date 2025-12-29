package io.mawhebty.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthenticationManagerEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> errorBody = new HashMap<>();
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("code", "UNAUTHORIZED");
        errorDetails.put("message", authException.getMessage());
        errorBody.put("error", errorDetails);

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
