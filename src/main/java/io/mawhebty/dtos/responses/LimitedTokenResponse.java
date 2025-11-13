package io.mawhebty.dtos.responses;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LimitedTokenResponse {
    private String limitedToken;
    private String tokenType;
    private Long expiresIn;
    private List<String> permissions;
    private String userStatus;
    private String message;
    private String userRole;
}
