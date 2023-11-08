package io.hkfullstack.securecapita.utils;

import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hkfullstack.securecapita.exception.ApiException;
import io.hkfullstack.securecapita.model.SecureApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class ExceptionUtils {
    public static void processError(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        SecureApiResponse apiResponse;
        if(exception instanceof ApiException || exception instanceof DisabledException ||
                exception instanceof BadCredentialsException || exception instanceof LockedException ||
                exception instanceof JWTVerificationException) {
            apiResponse = getSecureApiResponse(response, exception.getMessage(), BAD_REQUEST);
        } else {
            apiResponse = getSecureApiResponse(response, "An error occurred. Please try again.", INTERNAL_SERVER_ERROR);
        }
        writeResponse(response, apiResponse);
    }

    private static void writeResponse(HttpServletResponse response, SecureApiResponse apiResponse) {
        try {
            OutputStream outputStream = response.getOutputStream();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(outputStream, apiResponse);
            outputStream.flush();
        } catch(Exception exception) {
            exception.printStackTrace();
        }

    }

    private static SecureApiResponse getSecureApiResponse(HttpServletResponse response, String message, HttpStatus httpStatus) {
        SecureApiResponse secureApiResponse = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason(message)
                .status(httpStatus)
                .statusCode(httpStatus.value())
                .build();
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(httpStatus.value());
        return secureApiResponse;
    }
}
