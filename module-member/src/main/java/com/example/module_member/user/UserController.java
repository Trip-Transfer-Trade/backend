package com.example.module_member.user;

import com.example.module_member.dto.LoginRequestDto;
import com.example.module_member.dto.LoginResponseDto;
import com.example.module_member.dto.SignUpRequestDto;
import com.example.module_utility.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody SignUpRequestDto request) {
        return ResponseEntity.ok(userService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/test-auth")
    public ResponseEntity<String> testAuth(@RequestHeader(value = "Authorization", required = false) String token,
                                           @RequestHeader(value = "X-Authenticated-User", required = false) String username) {
        return ResponseEntity.ok("토큰: " + token + " 사용자 ID: " + username);
    }
}
