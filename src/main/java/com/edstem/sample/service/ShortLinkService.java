package com.edstem.sample.service;

import com.edstem.sample.dto.request.ShortenRequest;
import com.edstem.sample.dto.response.CreationResult;
import com.edstem.sample.dto.response.ShortLinkResponse;
import com.edstem.sample.dto.response.ShortLinkStatsResponse;
import com.edstem.sample.entity.ShortLink;
import com.edstem.sample.exception.ApplicationException;
import com.edstem.sample.exception.LinkExpiredException;
import com.edstem.sample.exception.ResourceNotFoundException;
import com.edstem.sample.mapper.ShortLinkMapper;
import com.edstem.sample.repository.ShortLinkRepository;
import java.time.Clock;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShortLinkService {

  private static final int MAX_CODE_ATTEMPTS = 10;

  private final ShortLinkRepository shortLinkRepository;
  private final ShortLinkMapper shortLinkMapper;
  private final ShortCodeGenerator codeGenerator;
  private final Clock clock;

  @Transactional
  public CreationResult<ShortLinkResponse> shorten(ShortenRequest request) {
    return shortLinkRepository
        .findFirstByOriginalUrlAndExpiresAt(request.url(), request.expiresAt())
        .map(existing -> CreationResult.existing(shortLinkMapper.toResponse(existing)))
        .orElseGet(() -> CreationResult.created(shortLinkMapper.toResponse(create(request))));
  }

  @Transactional
  public String resolve(String code) {
    ShortLink link = findOrThrow(code);
    if (link.isExpired(clock.instant())) {
      throw new LinkExpiredException(code);
    }
    shortLinkRepository.incrementVisitCount(link.getId());
    return link.getOriginalUrl();
  }

  public ShortLinkStatsResponse stats(String code) {
    return shortLinkMapper.toStats(findOrThrow(code));
  }

  private ShortLink create(ShortenRequest request) {
    return shortLinkRepository.saveAndFlush(shortLinkMapper.toEntity(request, uniqueCode()));
  }

  private String uniqueCode() {
    return Stream.generate(codeGenerator::next)
        .limit(MAX_CODE_ATTEMPTS)
        .filter(code -> !shortLinkRepository.existsByCode(code))
        .findFirst()
        .orElseThrow(CodeSpaceExhaustedException::new);
  }

  private ShortLink findOrThrow(String code) {
    return shortLinkRepository
        .findByCode(code)
        .orElseThrow(() -> new ResourceNotFoundException(ShortLink.class, code));
  }

  private static final class CodeSpaceExhaustedException extends ApplicationException {
    private CodeSpaceExhaustedException() {
      super(
          "CODE_GENERATION_FAILED",
          "Could not generate a unique short code",
          HttpStatus.SERVICE_UNAVAILABLE);
    }
  }
}
