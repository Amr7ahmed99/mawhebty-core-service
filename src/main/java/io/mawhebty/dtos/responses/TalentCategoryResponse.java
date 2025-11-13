package io.mawhebty.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TalentCategoryResponse {
    Integer id; // reference to partnerId
    String nameEn;
    String nameAr;
    List<TalentSubCategoryResponse> subCategories;
}
