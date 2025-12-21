package io.mawhebty.dtos.requests;

import io.mawhebty.validations.FileSize;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Please provide phone number")
    private String phone;

    @NotBlank(message = "Please provide prefix code for phone number")
    private String prefixCode;

//    @NotBlank(message = "Password is required")
//    @Size(min = 6, message = "Password must be at least 6 characters")
//    private String password;

    @NotNull
    @Min(1)
    private Integer roleId; // Reference to user_role.id

    private Integer userTypeId; // Reference to user_type.id

    @NotNull
    @Min(1)
    private Integer categoryId; // Reference to talent_category.id

    private Integer subCategoryId; // Reference to talent_sub_category.id

    private Integer participationTypeId;// project_idea, personal_talent, patent

    @NotBlank
    @Size(max = 250)
    private String shortBio;

    private Long gender;

//    @FileSize(max = 50 * 1024 * 1024*10) // 50 MB
    private MultipartFile file; // Video/Image/Document
    
    private String firstName;
    
    private String lastName;

    private Map<Integer, Object> talentCategoryFormMap;// refers to map[formKeyId, formValue]

    private String talentCategoryForm;

    @NotBlank
    private String country;
    @NotBlank
    private String city;
    private Integer age;
    
    // For company researchers
    private String companyName;
    private String commercialRegNo;
    private String contactPerson;
}
