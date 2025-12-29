package io.mawhebty.security;

import io.mawhebty.enums.UserStatusEnum;
import io.mawhebty.models.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class CustomUserDetails implements UserDetails {

    @Getter
    @Setter
    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (user.getRole() != null) {
            String roleName = user.getRole().getName().name();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName.toUpperCase();
            }
            authorities.add(new SimpleGrantedAuthority(roleName));
        }

        if (user.getStatus() != null && UserStatusEnum.ACTIVE.getName().equals(user.getStatus().getName())) {
            authorities.add(new SimpleGrantedAuthority("USER_ACTIVE"));
        }

        if (user.getIsVerified() != null && user.getIsVerified()) {
            authorities.add(new SimpleGrantedAuthority("USER_VERIFIED"));
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (user.getStatus() != null) {
            return !UserStatusEnum.REJECTED.getName().equals(user.getStatus().getName()) &&
                    !UserStatusEnum.SUSPENDED.getName().equals(user.getStatus().getName());
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (user.getStatus() != null) {
            return UserStatusEnum.ACTIVE.getName().equals(user.getStatus().getName()) ||
                    UserStatusEnum.PENDING_MODERATION.getName().equals(user.getStatus().getName());
        }
        return true;
    }

    public Long getUserId() {
        return user.getId();
    }
}