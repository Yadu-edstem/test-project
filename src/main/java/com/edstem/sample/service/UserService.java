package com.edstem.sample.service;

import com.edstem.sample.dto.request.CredentialsRequest;
import com.edstem.sample.dto.response.TokenResponse;
import com.edstem.sample.dto.response.UserResponse;
import com.edstem.sample.entity.AppUser;
import com.edstem.sample.entity.Role;
import com.edstem.sample.exception.BusinessRuleException;
import com.edstem.sample.exception.InvalidCredentialsException;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.mapper.UserMapper;
import com.edstem.sample.repository.AppUserRepository;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final AppUserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final TokenService tokenService;

  @Transactional
  public UserResponse register(CredentialsRequest request) {
    String email = normalize(request.email());
    if (userRepository.existsByEmail(email)) {
      throw new BusinessRuleException("EMAIL_TAKEN", "An account with this email already exists");
    }
    return userMapper.toResponse(save(email, request.password(), Role.USER));
  }

  @Transactional
  public boolean createAdminIfAbsent(String rawEmail, String rawPassword) {
    String email = normalize(rawEmail);
    if (userRepository.existsByEmail(email)) {
      return false;
    }
    save(email, rawPassword, Role.ADMIN);
    return true;
  }

  public TokenResponse login(CredentialsRequest request) {
    return userRepository
        .findByEmail(normalize(request.email()))
        .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
        .map(tokenService::issueAccessToken)
        .orElseThrow(InvalidCredentialsException::new);
  }

  public UserResponse get(UUID id) {
    return userRepository
        .findById(id)
        .map(userMapper::toResponse)
        .orElseThrow(() -> new ResourceNotFoundException(AppUser.class, id));
  }

  public Page<UserResponse> list(Pageable pageable) {
    return userRepository.findAll(pageable).map(userMapper::toResponse);
  }

  private AppUser save(String email, String rawPassword, Role role) {
    return userRepository.saveAndFlush(
        userMapper.toEntity(email, passwordEncoder.encode(rawPassword), role));
  }

  private static String normalize(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
