package io.hkfullstack.securecapita.exception;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import io.hkfullstack.securecapita.model.SecureApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalTime.now;
import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class HandleException extends ResponseEntityExceptionHandler implements ErrorController {
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason(exception.getMessage()) // In a real-world project, customize this message
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(resolve(statusCode.value()))
                .statusCode(statusCode.value())
                .build();
        return new ResponseEntity<>(response, statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        String fieldErrorMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason(fieldErrorMessage)
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(resolve(statusCode.value()))
                .statusCode(statusCode.value())
                .build();
        return new ResponseEntity<>(response, statusCode);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<SecureApiResponse> sQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason(exception.getMessage())
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<SecureApiResponse> badCredentialsException(BadCredentialsException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason(exception.getMessage())
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<SecureApiResponse> apiException(ApiException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason(exception.getMessage())
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<SecureApiResponse> accessDeniedException(AccessDeniedException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason("Access Denied. You dont have access")
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(FORBIDDEN)
                .statusCode(FORBIDDEN.value())
                .build();
        return new ResponseEntity<>(response, FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SecureApiResponse> exception(Exception exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason("Some error occurred")
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(INTERNAL_SERVER_ERROR)
                .statusCode(INTERNAL_SERVER_ERROR.value())
                .build();
        return new ResponseEntity<>(response, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<SecureApiResponse> emptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason(exception.getMessage().contains("expected 1, actual 0") ? "Record Not Found" : exception.getMessage())
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(NOT_FOUND)
                .statusCode(NOT_FOUND.value())
                .build();
        return new ResponseEntity<>(response, NOT_FOUND);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<SecureApiResponse> disabledException(DisabledException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason("Account is disabled.")
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(NOT_ACCEPTABLE)
                .statusCode(NOT_ACCEPTABLE.value())
                .build();
        return new ResponseEntity<>(response, NOT_ACCEPTABLE);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<SecureApiResponse> lockedException(LockedException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason("Account is locked.")
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(NOT_ACCEPTABLE)
                .statusCode(NOT_ACCEPTABLE.value())
                .build();
        return new ResponseEntity<>(response, NOT_ACCEPTABLE);
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<SecureApiResponse> signatureVerificationException(SignatureVerificationException exception) {
        log.error(exception.getMessage());
        SecureApiResponse response = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason("Invalid Token")
                .developerMessage(exception.getMessage()) // In a real-world project, customize this message
                .status(BAD_REQUEST)
                .statusCode(BAD_REQUEST.value())
                .build();
        return new ResponseEntity<>(response, BAD_REQUEST);
    }
}
