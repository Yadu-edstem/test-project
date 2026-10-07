package com.edstem.sample.controller;

import com.edstem.sample.dto.ApiResponse;
import com.edstem.sample.dto.response.UserResponse;
import com.edstem.sample.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "Profiles; listing is admin-only")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "The logged-in user's profile")
  @GetMapping("/me")
  public ApiResponse<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
    return ApiResponse.success(userService.get(UUID.fromString(jwt.getSubject())));
  }

  @Operation(summary = "List all users (ADMIN only)")
  @GetMapping
  public ApiResponse<Page<UserResponse>> list(
      @PageableDefault(sort = "createdAt") Pageable pageable) {
    return ApiResponse.success(userService.list(pageable));
  }
}
