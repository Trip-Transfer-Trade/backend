package com.example.module_member.user;

import com.example.module_member.dto.LoginRequestDto;
import com.example.module_member.dto.LoginResponseDto;
import com.example.module_member.dto.SignUpRequestDto;
import com.example.module_member.dto.UserResponseDto;
import com.example.module_utility.response.Response;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<Response<UserResponseDto>> findUserByUsername(@RequestHeader(value = "X-Authenticated-User", required = false) String username) {
        UserResponseDto response = userService.findUserByUsername(username);
        return ResponseEntity.ok(Response.success(response));
    }

    @PostMapping("/signup")
    public ResponseEntity<Response<Void>> signUp(@RequestBody SignUpRequestDto request) {
        return ResponseEntity.ok(userService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponseDto>> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        Response<LoginResponseDto> loginResponse = userService.login(request);

        if (loginResponse.getStatus() != 200) {
            return ResponseEntity.status(loginResponse.getStatus()).body(loginResponse);
        }

        String token = loginResponse.getData().getToken();
        setJwtCookie(response, token);

        return ResponseEntity.ok(loginResponse);
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 10);
        response.addCookie(cookie);
    }

    @GetMapping("/test-auth")
    public ResponseEntity<Response<String>> testAuth(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestHeader(value = "X-Authenticated-User", required = false) String username) {
        String message = "토큰: " + token + " 사용자 ID: " + username;
        return ResponseEntity.ok(Response.success(message));
    }
}
