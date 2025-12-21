package io.mawhebty.controllers;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.mawhebty.services.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("validate-email")
    public ResponseEntity<Map<Object,Object>> validateUserEmail(@RequestParam String email) {
        return ResponseEntity.ok().body(Map.of("isExist", this.userService.validateEmail(email)));
    }
    
    @GetMapping("validate-phone")
    public ResponseEntity<Map<Object,Object>> validateUserPhone(@RequestParam String phone) {
        return ResponseEntity.ok().body(Map.of("isExist", this.userService.validatePhone(phone)));
    }
    
}
