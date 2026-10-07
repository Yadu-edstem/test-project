package com.edstem.sample.mapper;

import com.edstem.sample.dto.response.UserResponse;
import com.edstem.sample.entity.AppUser;
import com.edstem.sample.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public AppUser toEntity(String email, String passwordHash, Role role) {
    return AppUser.builder().email(email).passwordHash(passwordHash).role(role).build();
  }

  public UserResponse toResponse(AppUser user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
  }
}
