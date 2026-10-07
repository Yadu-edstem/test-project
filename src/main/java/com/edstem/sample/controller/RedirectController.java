package com.edstem.sample.controller;

import com.edstem.sample.service.ShortLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Short links")
@RestController
@RequiredArgsConstructor
public class RedirectController {

  private final ShortLinkService shortLinkService;

  @Operation(summary = "Redirect a short code to its original URL and count the visit")
  @GetMapping("/s/{code}")
  public ResponseEntity<Void> redirect(@PathVariable String code) {
    return ResponseEntity.status(HttpStatus.FOUND)
        .location(URI.create(shortLinkService.resolve(code)))
        .build();
  }
}
