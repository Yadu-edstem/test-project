package com.edstem.sample.controller;

import com.edstem.sample.dto.ApiResponse;
import com.edstem.sample.dto.request.ShortenRequest;
import com.edstem.sample.dto.response.CreationResult;
import com.edstem.sample.dto.response.ShortLinkResponse;
import com.edstem.sample.dto.response.ShortLinkStatsResponse;
import com.edstem.sample.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Short links", description = "Shorten URLs and read visit statistics")
@RestController
@RequestMapping("/api/v1/links")
@RequiredArgsConstructor
public class ShortLinkController {

  private final ShortLinkService shortLinkService;

  @Operation(
      summary = "Shorten a URL",
      description =
          "Returns 201 with a new link, or 200 with the existing link when the same URL was already"
              + " shortened with the same expiry.")
  @PostMapping
  public ResponseEntity<ApiResponse<ShortLinkResponse>> shorten(
      @Valid @RequestBody ShortenRequest request) {
    CreationResult<ShortLinkResponse> result = shortLinkService.shorten(request);
    return ResponseEntity.status(result.status()).body(ApiResponse.success(result.value()));
  }

  @Operation(summary = "Visit statistics for a short code")
  @GetMapping("/{code}/stats")
  public ApiResponse<ShortLinkStatsResponse> stats(@PathVariable String code) {
    return ApiResponse.success(shortLinkService.stats(code));
  }
}
