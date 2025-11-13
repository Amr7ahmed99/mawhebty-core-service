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
public class DraftResponse {
    private Long userId;
    private String fileUrl;
    private String message;
    private TokenResponse tokenResponse;
    
    public static DraftResponse success(Long userId, String fileUrl) {
        return DraftResponse.builder()
                .userId(userId)
                .fileUrl(fileUrl)
                .message("Draft registration successful")
                .build();
    }

    // for individual researcher
    public static DraftResponse successWithoutFileAndWithToken(Long userId, TokenResponse tokenResponse) {
        return DraftResponse.builder()
                .userId(userId)
                .message("registration successful, researcher is active now")
                .tokenResponse(tokenResponse)
                .build();
    }
}
