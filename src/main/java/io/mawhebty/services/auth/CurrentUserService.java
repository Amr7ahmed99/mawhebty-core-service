package io.mawhebty.services.auth;

import io.mawhebty.models.User;
import io.mawhebty.security.CustomUserDetails;
import io.mawhebty.support.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final MessageService messageService; // Added

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException(
                    messageService.getMessage("user.not.authenticated")
            );
        }

        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

    public Long getCurrentUserId(Authentication authentication) {
        return getCurrentUser(authentication).getId();
    }
}