package com.example.module_exchange.clients;

import com.example.module_member.dto.UserResponseDto;
import com.example.module_utility.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name="module-member")
public interface MemberClient {
    @GetMapping("/api/members")
    ResponseEntity<Response<UserResponseDto>> findUserByUsername(@RequestHeader(value = "X-Authenticated-Username",required = false) String username);
}
