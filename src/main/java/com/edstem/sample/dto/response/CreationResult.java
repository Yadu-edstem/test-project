package com.edstem.sample.dto.response;

import org.springframework.http.HttpStatus;

public record CreationResult<T>(T value, boolean newlyCreated) {

  public static <T> CreationResult<T> created(T value) {
    return new CreationResult<>(value, true);
  }

  public static <T> CreationResult<T> existing(T value) {
    return new CreationResult<>(value, false);
  }

  public HttpStatus status() {
    return newlyCreated ? HttpStatus.CREATED : HttpStatus.OK;
  }
}
