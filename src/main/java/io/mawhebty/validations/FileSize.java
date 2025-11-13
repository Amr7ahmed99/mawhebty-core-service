package io.mawhebty.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileSizeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FileSize {

    String message() default "File size exceeds the allowed limit";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // maximum in bytes
    long max();
}
