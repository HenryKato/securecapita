package io.hkfullstack.securecapita.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hkfullstack.securecapita.model.SecureApiResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        SecureApiResponse secureApiResponse = SecureApiResponse.builder()
                .timestamp(now().toString())
                .reason("You are not authorized..." + authException.getMessage())
                .status(UNAUTHORIZED)
                .statusCode(UNAUTHORIZED.value())
                .build();

        response.setContentType(APPLICATION_JSON_VALUE);
        OutputStream outputStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, secureApiResponse);
        outputStream.flush();
    }
}
