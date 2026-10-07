package com.edstem.sample.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApplicationException {
  private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

  public ResourceNotFoundException(Class<?> resource, Object id) {
    super(ERROR_CODE, resource.getSimpleName() + " not found with ID: " + id, HttpStatus.NOT_FOUND);
  }
}
