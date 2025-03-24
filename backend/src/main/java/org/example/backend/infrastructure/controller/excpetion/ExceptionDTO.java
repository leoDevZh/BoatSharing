package org.example.backend.infrastructure.controller.excpetion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@Builder
public class ExceptionDTO {
    HttpStatus httpStatus;
    String message;
}
