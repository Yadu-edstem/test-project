package com.edstem.sample.exception;

import com.edstem.sample.dto.ApiResponse;
import com.edstem.sample.dto.ErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
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

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Void>> handleBinding(BindException e) {
    return validationFailed(
        e.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    fieldError -> messages(fieldError.getDefaultMessage()),
                    GlobalExceptionHandler::merge)));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
      ConstraintViolationException e) {
    return validationFailed(
        e.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> messages(violation.getMessage()),
                    GlobalExceptionHandler::merge)));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodValidation(
      HandlerMethodValidationException e) {
    return validationFailed(
        e.getParameterValidationResults().stream()
            .collect(
                Collectors.toMap(
                    result -> result.getMethodParameter().getParameterName(),
                    result ->
                        result.getResolvableErrors().stream()
                            .map(MessageSourceResolvable::getDefaultMessage)
                            .toArray(String[]::new),
                    GlobalExceptionHandler::merge)));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException e) {
    if (e.getCause() instanceof InvalidFormatException invalid && !invalid.getPath().isEmpty()) {
      String field = invalid.getPath().get(invalid.getPath().size() - 1).getFieldName();
      return validationFailed(Map.of(field, messages(invalidValueMessage(invalid))));
    }
    log.warn("Unreadable request body: {}", e.getMessage());
    return respond(HttpStatus.BAD_REQUEST, ERROR_CODE_BAD_REQUEST, "Malformed request body");
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
      MethodArgumentTypeMismatchException e) {
    return validationFailed(Map.of(e.getName(), messages("Invalid value '" + e.getValue() + "'")));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
      MissingServletRequestParameterException e) {
    return validationFailed(Map.of(e.getParameterName(), messages("is required")));
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
    return validationFailed(Map.of(e.getHeaderName(), messages("header is required")));
  }

  @ExceptionHandler(PropertyReferenceException.class)
  public ResponseEntity<ApiResponse<Void>> handleUnknownProperty(PropertyReferenceException e) {
    return validationFailed(
        Map.of("sort", messages("Unknown property '" + e.getPropertyName() + "'")));
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

  @ExceptionHandler(ServletException.class)
  public ResponseEntity<ApiResponse<Void>> handleServletException(ServletException e) {
    if (e instanceof org.springframework.web.ErrorResponse webError) {
      HttpStatus status = HttpStatus.valueOf(webError.getStatusCode().value());
      log.warn("Request rejected with {}: {}", status.value(), e.getMessage());
      return respond(status, status.name(), status.getReasonPhrase());
    }
    return handleUnexpected(e);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
    log.error("Unexpected error", e);
    return respond(
        HttpStatus.INTERNAL_SERVER_ERROR, ERROR_CODE_INTERNAL, "An unexpected error occurred");
  }

  private static String invalidValueMessage(InvalidFormatException invalid) {
    Class<?> target = invalid.getTargetType();
    if (target != null && target.isEnum()) {
      return "Invalid value '"
          + invalid.getValue()
          + "', accepted values: "
          + Arrays.toString(target.getEnumConstants());
    }
    return "Invalid value '" + invalid.getValue() + "'";
  }

  private static String[] messages(String message) {
    return new String[] {message};
  }

  private static String[] merge(String[] first, String[] second) {
    return Stream.concat(Arrays.stream(first), Arrays.stream(second)).toArray(String[]::new);
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
