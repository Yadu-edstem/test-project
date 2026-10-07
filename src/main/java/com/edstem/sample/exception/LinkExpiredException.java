package com.edstem.sample.exception;

import org.springframework.http.HttpStatus;

public class LinkExpiredException extends ApplicationException {
  private static final String ERROR_CODE = "LINK_EXPIRED";

  public LinkExpiredException(String code) {
    super(ERROR_CODE, "Short link '" + code + "' has expired", HttpStatus.GONE);
  }
}
