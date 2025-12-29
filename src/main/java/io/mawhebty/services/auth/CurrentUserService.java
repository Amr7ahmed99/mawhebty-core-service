package io.mawhebty.services.auth;

import io.mawhebty.exceptions.UserNotFoundException;
import io.mawhebty.models.User;
import io.mawhebty.repository.UserRepository;
import io.mawhebty.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User not authenticated");
        }

        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

    public Long getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getId();
    }
}