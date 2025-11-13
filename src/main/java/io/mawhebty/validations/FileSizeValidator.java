package io.mawhebty.validations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

public class FileSizeValidator implements ConstraintValidator<FileSize, MultipartFile> {

    private long maxSize;

    @Override
    public void initialize(FileSize constraintAnnotation) {
        this.maxSize = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // the @NotNull will handle the null case
        if (file == null || file.isEmpty()) {
            return true;
        }

        return file.getSize() <= maxSize;
    }
}
