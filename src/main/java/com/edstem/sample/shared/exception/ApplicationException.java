package com.edstem.sample.shared.exception;

import java.util.Map;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApplicationException extends RuntimeException {
  private final String errorCode;
  private final HttpStatus httpStatus;

  protected ApplicationException(String errorCode, String message, HttpStatus httpStatus) {
    super(message);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  protected ApplicationException(
      String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.httpStatus = httpStatus;
  }

  public Map<String, Object> getErrorData() {
    return Map.of();
  }
}
