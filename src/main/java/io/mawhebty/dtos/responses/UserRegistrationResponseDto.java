package io.mawhebty.dtos.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRegistrationResponseDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("last_name")
    private String lastName;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("user_status")
    private Long userStatus;
    @JsonProperty("user_role")
    private Integer userRole;
    @JsonProperty("user_type")
    private Integer userType;// Individual/Company
    @JsonProperty("short_bio")
    private String shortBio;
    @JsonProperty("image_url")
    private String imageUrl;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("contact_person")
    private String contactPerson;
    @JsonProperty("commercial_reg_no")
    private String commercialRegNo;
    @JsonProperty("permissions")
    private List<Integer> permissions;
}
