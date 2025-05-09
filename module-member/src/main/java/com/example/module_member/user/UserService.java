package com.example.module_member.user;

import com.example.module_member.dto.*;
import com.example.module_utility.response.Response;
import com.example.module_member.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public Response<Void> signUp(SignUpRequestDto request) {
        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
            return Response.error(400, "이미 존재하는 아이디입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = request.toEntity(encodedPassword);
        userRepository.save(user);

        return Response.successWithoutData();
    }

    public Response<LoginResponseDto> login(LoginRequestDto request) {
        User user = userRepository.findByUserName(request.getUserName())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Response.error(401, "아이디 혹은 비밀번호가 잘못 입력되었습니다.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUserName());
        return Response.success(new LoginResponseDto(token));
    }

    public UserResponseDto findUserByUsername(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        return UserResponseDto.fromEntity(user);
    }

    public Response<UserInfoResponseDTO> getUserInfo(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return Response.success(UserInfoResponseDTO.basicInfoFromEntity(user));
    }
}
