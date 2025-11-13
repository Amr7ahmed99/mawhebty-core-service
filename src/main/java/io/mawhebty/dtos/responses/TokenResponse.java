package io.mawhebty.dtos.responses;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private List<Integer> permissions;
    private String userStatus;
    private String userRole;
}