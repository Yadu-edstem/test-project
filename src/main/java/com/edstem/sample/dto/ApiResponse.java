package com.edstem.sample.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
  private boolean success;
  private T data;
  private ErrorResponse error;

  public static <T> ApiResponse<T> success(T data) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(true);
    response.setData(data);
    return response;
  }

  public static <T> ApiResponse<T> error(ErrorResponse error) {
    ApiResponse<T> response = new ApiResponse<>();
    response.setSuccess(false);
    response.setError(error);
    return response;
  }
}
