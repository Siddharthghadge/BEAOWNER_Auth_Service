package com.carrental.authservice.client;

import com.carrental.authservice.dto.CreateUserRequest;
import com.carrental.authservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "userservice", url = "${USER_SERVICE_URL}")
public interface UserServiceClient {

    @PostMapping("/api/users/register/internal")
    UserResponse registerInternal(@RequestBody CreateUserRequest request);

    @GetMapping("/api/users/email/{email}")
    UserResponse getUserByEmail(@PathVariable("email") String email);
}
