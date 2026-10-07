package com.edstem.sample.service;

import com.edstem.sample.config.SecurityConfig;
import com.edstem.sample.config.SecurityProperties;
import com.edstem.sample.dto.response.TokenResponse;
import com.edstem.sample.entity.AppUser;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

  private static final String ISSUER = "sample";

  private final JwtEncoder jwtEncoder;
  private final SecurityProperties properties;
  private final Clock clock;

  public TokenResponse issueAccessToken(AppUser user) {
    Instant now = clock.instant();
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(ISSUER)
            .subject(user.getId().toString())
            .issuedAt(now)
            .expiresAt(now.plus(properties.accessTokenTtl()))
            .claim(SecurityConfig.ROLES_CLAIM, List.of(user.getRole().name()))
            .build();
    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return TokenResponse.bearer(token, properties.accessTokenTtl().toSeconds());
  }
}
