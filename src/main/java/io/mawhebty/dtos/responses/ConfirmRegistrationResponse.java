package io.mawhebty.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ConfirmRegistrationResponse {
    private String message;
    private TokenResponse tokenResponse;

    public static ConfirmRegistrationResponse successWithoutFileAndWithToken(Long userId, TokenResponse tokenResponse) {
        return ConfirmRegistrationResponse.builder()
                .message("registration successful, researcher is active now")
                .tokenResponse(tokenResponse)
                .build();
    }
}
