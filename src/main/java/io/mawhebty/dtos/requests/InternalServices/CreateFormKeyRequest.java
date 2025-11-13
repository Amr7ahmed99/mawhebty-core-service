package io.mawhebty.dtos.requests.InternalServices;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateFormKeyRequest {
    @NotNull
    private Integer categoryPartnerId;

    private Integer subCategoryPartnerId; // nullable

    @NotNull
    private Integer fieldKeyId;
    @NotBlank
    private String nameEn;
    @NotBlank
    private String nameAr;
    @NotBlank
    private String fieldType;
    @Builder.Default
    private boolean required= false;
}

