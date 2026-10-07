package com.edstem.sample.dto.response;

import com.edstem.sample.entity.Role;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, Role role, Instant createdAt) {}
