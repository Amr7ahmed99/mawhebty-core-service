package io.mawhebty.dtos.requests.InternalServices;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTalentCategoryRequest {
    @NotNull
    @Min(1)
    private Integer partnerId;
    @NotBlank
    private String nameEn;
    @NotBlank
    private String nameAr;
    @NotNull
    @Min(1)
    private Integer participationTypeId;// project_idea, personal_talent, patent


}
