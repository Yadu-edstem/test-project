package com.edstem.sample.controller;

import com.edstem.sample.dto.ApiResponse;
import com.edstem.sample.dto.request.CredentialsRequest;
import com.edstem.sample.dto.response.TokenResponse;
import com.edstem.sample.dto.response.UserResponse;
import com.edstem.sample.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Register and log in")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;

  @Operation(summary = "Register a USER account")
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<UserResponse> register(@Valid @RequestBody CredentialsRequest request) {
    return ApiResponse.success(userService.register(request));
  }

  @Operation(summary = "Log in and receive a 15-minute bearer token")
  @PostMapping("/login")
  public ApiResponse<TokenResponse> login(@Valid @RequestBody CredentialsRequest request) {
    return ApiResponse.success(userService.login(request));
  }
}
