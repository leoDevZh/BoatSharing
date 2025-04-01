package org.example.backend.infrastructure.controller.excpetion;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionDTO> handleMethodArgumentNotValidExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionDTO.builder().httpStatus(HttpStatus.BAD_REQUEST).message("Provide a valid Requestbody").build());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionDTO> methodArgumentTypeMismatchExceptionException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ExceptionDTO.builder().httpStatus(HttpStatus.BAD_REQUEST).message("Provide a valid Variable").build());
    }

    @ExceptionHandler(CannotAcquireLockException.class)
    public ResponseEntity<ExceptionDTO> handleCannotAcquireLockException(CannotAcquireLockException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ExceptionDTO.builder().httpStatus(HttpStatus.CONFLICT).message("Resource is currently locked - try again").build());
    }

    @ExceptionHandler
    public ResponseEntity<ExceptionDTO> handleBadCredentialException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ExceptionDTO.builder().httpStatus(HttpStatus.UNAUTHORIZED).message("Bad credentials").build());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDTO> handleUnknownExceptions(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionDTO.builder().httpStatus(HttpStatus.INTERNAL_SERVER_ERROR).message("Some unexpected Exception happened").build());
    }
}
