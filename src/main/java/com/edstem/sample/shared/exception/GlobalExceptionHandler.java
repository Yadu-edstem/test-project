package com.edstem.sample.shared.exception;

import com.edstem.sample.shared.dto.ApiResponse;
import com.edstem.sample.shared.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  private static final String ERROR_CODE_VALIDATION = "VALIDATION_ERROR";
  private static final String ERROR_CODE_BAD_REQUEST = "BAD_REQUEST";
  private static final String ERROR_CODE_NOT_FOUND = "NOT_FOUND";
  private static final String ERROR_CODE_CONFLICT = "CONFLICT";
  private static final String ERROR_CODE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
  private static final String ERROR_CODE_INTERNAL = "INTERNAL_ERROR";
  private static final String MESSAGE_VALIDATION_FAILED = "Validation failed";

  @ExceptionHandler(ApplicationException.class)
  public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException e) {
    if (e.getHttpStatus().is4xxClientError()) {
      log.warn("ApplicationException caught: {} - {}", e.getErrorCode(), e.getMessage());
    } else {
      log.error("ApplicationException caught: {} - {}", e.getErrorCode(), e.getMessage(), e);
    }
    Map<String, Object> errorData = e.getErrorData();
    ErrorResponse error =
        ErrorResponse.builder()
            .code(e.getErrorCode())
            .message(e.getMessage())
            .data(errorData.isEmpty() ? null : errorData)
            .build();
    return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.error(error));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
    Map<String, String[]> details = new HashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(
            fieldError ->
                details.put(fieldError.getField(), new String[] {fieldError.getDefaultMessage()}));
    return validationFailed(details);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
      ConstraintViolationException e) {
    Map<String, String[]> details = new HashMap<>();
    e.getConstraintViolations()
        .forEach(
            violation ->
                details.put(
                    violation.getPropertyPath().toString(), new String[] {violation.getMessage()}));
    return validationFailed(details);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
    log.warn("Bad request: {}", e.getMessage());
    return respond(HttpStatus.BAD_REQUEST, ERROR_CODE_BAD_REQUEST, "Malformed request");
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
    return respond(HttpStatus.NOT_FOUND, ERROR_CODE_NOT_FOUND, "Resource not found");
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException e) {
    return respond(
        HttpStatus.METHOD_NOT_ALLOWED, ERROR_CODE_METHOD_NOT_ALLOWED, "Method not allowed");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
    log.warn("Data integrity violation: {}", e.getMostSpecificCause().getClass().getSimpleName());
    return respond(
        HttpStatus.CONFLICT, ERROR_CODE_CONFLICT, "Request conflicts with existing data");
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
    log.error("Unexpected error", e);
    return respond(
        HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CODE_INTERNAL, "An unexpected error occurred");
  }

  private ResponseEntity<ApiResponse<Void>> validationFailed(Map<String, String[]> details) {
    ErrorResponse error =
        ErrorResponse.builder()
            .code(ERROR_CODE_VALIDATION)
            .message(MESSAGE_VALIDATION_FAILED)
            .details(details)
            .build();
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
  }

  private ResponseEntity<ApiResponse<Void>> respond(
      HttpStatus status, String code, String message) {
    ErrorResponse error = ErrorResponse.builder().code(code).message(message).build();
    return ResponseEntity.status(status).body(ApiResponse.error(error));
  }
}
