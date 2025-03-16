package com.example.module_member.user;

import com.example.module_member.dto.*;
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
    public ResponseEntity<Response<UserResponseDto>> findUserByUsername(@RequestHeader(value = "X-Authenticated-Username", required = false) String username) {
        UserResponseDto response = userService.findUserByUsername(username);
        return ResponseEntity.ok(Response.success(response));
    }
    @GetMapping("info")
    public ResponseEntity<Response<UserInfoResponseDTO>> info(@RequestHeader(value = "X-Authenticated-User", required = false) Integer userId) {
        return ResponseEntity.ok(userService.getUserInfo(userId));
    }

    @PostMapping("/signup")
    public ResponseEntity<Response<Void>> signUp(@RequestBody SignUpRequestDto request) {
        Response<Void> response = userService.signUp(request);
        if (response.getStatus() != 200) {
            return ResponseEntity.status(response.getStatus()).body(response);
        }

        return ResponseEntity.ok(response);
//        return ResponseEntity.ok(userService.signUp(request));
    }

    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponseDto>> login(@RequestBody LoginRequestDto request, HttpServletResponse response) {
        Response<LoginResponseDto> loginResponse = userService.login(request);

        System.out.println("🔍 로그인 응답 상태 코드: " + loginResponse.getStatus());
        System.out.println("🔍 로그인 응답 메시지: " + loginResponse.getMessage());

        if (loginResponse.getStatus() != 200) {
            return ResponseEntity.status(loginResponse.getStatus()).body(loginResponse);
        }


        String token = loginResponse.getData().getToken();
        setJwtCookie(response, token);

        return ResponseEntity.ok(loginResponse);
    }
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(HttpServletResponse response) {
        removeJwtCookie(response);
        return ResponseEntity.ok(Response.success(null));
    }

    @GetMapping("/status")
    public ResponseEntity<Boolean> LoginStatus(@CookieValue(value = "token", required = false) String token){
        if (token == null) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);
    }

    private void setJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 10);
        response.addCookie(cookie);
    }

    private void removeJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    @GetMapping("/test-auth")
    public ResponseEntity<Response<String>> testAuth(
            @RequestHeader("X-Authenticated-Username") String username,
            @RequestHeader("X-Authenticated-User") int userId) {
        String message = "사용자 ID: " + userId + " / 사용자 이름: " + username;
        return ResponseEntity.ok(Response.success(message));
    }
}
