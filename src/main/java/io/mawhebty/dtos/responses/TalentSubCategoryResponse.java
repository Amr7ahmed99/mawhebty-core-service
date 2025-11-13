package io.mawhebty.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TalentSubCategoryResponse {
    Integer id; // reference to partnerId
    String nameEn;
    String nameAr;
}
