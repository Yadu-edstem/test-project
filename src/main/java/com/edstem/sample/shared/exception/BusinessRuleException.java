package com.edstem.sample.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApplicationException {

  public BusinessRuleException(String errorCode, String message) {
    super(errorCode, message, HttpStatus.CONFLICT);
  }
}
