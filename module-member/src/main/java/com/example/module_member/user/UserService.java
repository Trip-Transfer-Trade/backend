package com.example.module_member.user;

import com.example.module_member.dto.LoginRequestDto;
import com.example.module_member.dto.LoginResponseDto;
import com.example.module_member.dto.SignUpRequestDto;
import com.example.module_utility.response.ApiResponse;
import com.example.module_member.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public ApiResponse<String> signUp(SignUpRequestDto request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = request.toEntity(encodedPassword);
        userRepository.save(user);

        return ApiResponse.success("회원가입 성공");
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new RuntimeException("아이디 혹은 비밀번호가 잘못 입력되었습니다."));

        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!isPasswordMatch) {
            throw new RuntimeException("아이디 혹은 비밀번호가 잘못 입력되었습니다.");
        }

        String token = jwtUtil.generateToken(user.getUserName());
        return new LoginResponseDto(token);
    }


}
