package com.edstem.sample.shared.dto;

import java.util.Map;
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
public class ErrorResponse {
  private String code;
  private String message;
  private Map<String, Object> data;
  private Map<String, String[]> details;
}
