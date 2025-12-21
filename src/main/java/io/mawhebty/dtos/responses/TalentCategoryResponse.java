package io.mawhebty.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TalentCategoryResponse {
    private Integer id; // reference to partnerId
    private String nameEn;
    private String nameAr;
    private Integer participationTypeId;// project_idea, personal_talent, patent
    private List<TalentSubCategoryResponse> subCategories;
}
