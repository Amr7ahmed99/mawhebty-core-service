package io.mawhebty.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private void logException(Exception ex, HttpStatus status) {
        log.error("Exception handled - Status: {}, Message: {}, Exception: {}", 
                 status, ex.getMessage(), ex.getClass().getSimpleName(), ex);
    }
    
    @ExceptionHandler({UserNotFoundException.class, ResourceNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFoundExceptions(RuntimeException ex) {
        logException(ex, HttpStatus.NOT_FOUND);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Resource Not Found"); 
        response.put("message", ex.getMessage());
        response.put("code", "NOT_FOUND");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({PhoneAlreadyExistsException.class, EmailAlreadyExistsException.class})
    public ResponseEntity<Map<String, Object>> handleConflictExceptions(RuntimeException ex) {
        logException(ex, HttpStatus.CONFLICT);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Resource Already Exists");
        response.put("message", ex.getMessage());
        response.put("code", "CONFLICT");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler({BadDataException.class, IllegalArgumentException.class, IllegalStateException.class, UserNotVerified.class})
    public ResponseEntity<Map<String, Object>> handleBadRequestExceptions(RuntimeException ex) {
        logException(ex, HttpStatus.BAD_REQUEST);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        response.put("code", "BAD_REQUEST");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({OTPNotFoundException.class, OTPExpiredException.class, OTPAlreadyUsedException.class, OTPGenerationFailedException.class})
    public ResponseEntity<Map<String, Object>> handleOtpExceptions(RuntimeException ex) {
        logException(ex, HttpStatus.BAD_REQUEST);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "OTP Validation Failed");
        response.put("message", ex.getMessage());
        response.put("code", "OTP_VALIDATION_FAILED");
        response.put("nextStep", "request_new_otp");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({UserStatusNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleConfigurationExceptions(RuntimeException ex) {
        logException(ex, HttpStatus.INTERNAL_SERVER_ERROR);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Configuration Error");
        response.put("message", ex.getMessage());
        response.put("code", "CONFIGURATION_ERROR");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // @ExceptionHandler({RuntimeException.class})
    // public ResponseEntity<Map<String, Object>> handleInternalServerError(RuntimeException ex) {
    //     Map<String, Object> response = new HashMap<>();
    //     response.put("timestamp", LocalDateTime.now());
    //     response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    //     // response.put("error", ex.getMessage());
    //     response.put("error", "error has been occured");
    //     response.put("message", "Something went wrong try again later");
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    // }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logException(ex, HttpStatus.BAD_REQUEST);
        
        // collect all invalid fields
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    fieldError -> fieldError.getDefaultMessage() != null ? 
                                fieldError.getDefaultMessage() : "Validation error",
                    (existing, replacement) -> existing + ", " + replacement
                ));

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "One or more fields are invalid");
        response.put("code", "VALIDATION_FAILED");
        response.put("fieldErrors", fieldErrors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unhandled exception: ", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred. Please try again later.");
        response.put("code", "INTERNAL_ERROR");
        
        if (isDevelopmentEnvironment()) {
            response.put("debugMessage", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: ", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "error occurred. Please try again later.");
        response.put("code", "RUNTIME_ERROR");
        
        if (isDevelopmentEnvironment()) {
            response.put("debugMessage", ex.getMessage());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private boolean isDevelopmentEnvironment() {
        String env = System.getProperty("spring.profiles.active", "development");
        return "development".equals(env) || "local".equals(env);
    }
}
