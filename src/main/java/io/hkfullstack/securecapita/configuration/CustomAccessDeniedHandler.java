package io.hkfullstack.securecapita.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hkfullstack.securecapita.model.ApiResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ApiResponse apiResponse = ApiResponse.builder()
                .timestamp(now().toString())
                .reason("You don't permission for this resource")
                .status(FORBIDDEN)
                .statusCode(FORBIDDEN.value())
                .build();

        response.setContentType(APPLICATION_JSON_VALUE);
        OutputStream outputStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, apiResponse);
        outputStream.flush();
    }
}
