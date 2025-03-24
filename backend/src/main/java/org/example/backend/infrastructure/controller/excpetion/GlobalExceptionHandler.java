package org.example.backend.infrastructure.controller.excpetion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDTO> handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionDTO.builder().httpStatus(HttpStatus.BAD_REQUEST).message("Provide a valid Requestbody").build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDTO> handleUnknownExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionDTO.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).message("Some unexpected Exception happened").build());
    }
}
