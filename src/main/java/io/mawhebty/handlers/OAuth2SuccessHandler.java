package io.mawhebty.handlers;

import io.mawhebty.dtos.FindOrCreateUserDto;
import io.mawhebty.dtos.responses.TokenResponse;
import io.mawhebty.services.JWTService;
import io.mawhebty.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Entry point for sign in with google
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Extract user info from Google
        String email = oAuth2User.getAttribute("email");
//        String name = oAuth2User.getAttribute("name");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        // Find or create user
        FindOrCreateUserDto result= userService.findOrCreateByEmail(email);

        // Generate tokens
        TokenResponse tokenResponse= jwtService.determineSuitableTokenResponse(result.getUser());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", result.getUser().getId());
        userData.put("email", result.getUser().getEmail());
        userData.put("firstName", firstName);
        userData.put("lastName", lastName);
        userData.put("provider", "GOOGLE");

//        // Set refresh token as HttpOnly cookie
//        ResponseCookie refreshCookie = jwtService.buildCookieForRefreshToken(tokenResponse.getRefreshToken());
//
//        response.addHeader("Set-Cookie", refreshCookie.toString());

        String userJson = objectMapper.writeValueAsString(userData);

        // Send access token & user info to frontend via redirect (safe in query param or fragment)
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/callback")
                .queryParam("token", tokenResponse!=null? tokenResponse.getAccessToken(): null)
                .queryParam("user", userJson)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}

