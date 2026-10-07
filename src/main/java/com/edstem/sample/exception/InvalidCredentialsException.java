package com.edstem.sample.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends ApplicationException {
  private static final String ERROR_CODE = "INVALID_CREDENTIALS";

  public InvalidCredentialsException() {
    super(ERROR_CODE, "Email or password is incorrect", HttpStatus.UNAUTHORIZED);
  }
}
