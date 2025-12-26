package io.mawhebty.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DraftResponse {
    private boolean success;
    private String message;
    private UserRegistrationResponseDto user;
    @JsonProperty("token_response")
    private TokenResponse tokenResponse;
    
    public static DraftResponse success(UserRegistrationResponseDto user, String fileUrl) {
        return DraftResponse.builder()
                .success(true)
                .user(user)
                .message("Draft registration successful")
                .build();
    }

    // for individual researcher
    public static DraftResponse successWithoutFileAndWithToken(UserRegistrationResponseDto user, TokenResponse tokenResponse) {
        return DraftResponse.builder()
                .success(true)
                .user(user)
                .message("registration successful, researcher is active now")
                .tokenResponse(tokenResponse)
                .build();
    }
}
