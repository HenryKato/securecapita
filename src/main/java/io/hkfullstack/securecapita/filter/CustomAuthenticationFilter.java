package io.hkfullstack.securecapita.filter;

import io.hkfullstack.securecapita.provider.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Map.*;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String[] PUBLIC_ROUTES = { "/users/login", "/users/verify", "/users/register" };
    private static final String HTTP_OPTIONS_METHOD = "OPTIONS";
    private final TokenProvider tokenProvider;
    protected static final String TOKEN_KEY = "token";
    protected static final String EMAIL_KEY = "email";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Map<String, String> requestValues = getRequestValues(request); // The token in the values here will have a prefix of "Bearer "
            String token = getToken(request); // This is actual token without the "Bearer " prefix
            if(tokenProvider.isTokenValid(requestValues.get(EMAIL_KEY), token)) {
                List<GrantedAuthority> authorities = tokenProvider.getUserClaimsFromToken(requestValues.get(TOKEN_KEY));
                Authentication authentication = tokenProvider.getAuthentication(requestValues.get(EMAIL_KEY), authorities, request);
                SecurityContextHolder.getContext().setAuthentication(authentication); // Authenticated user
            } else { SecurityContextHolder.clearContext(); }
            filterChain.doFilter(request, response); // Let the request proceed to the next filter(s)
        } catch (Exception ex) {
            log.error(ex.getMessage());
//          processError(request, response, ex);
        }
    }

    @Override // Helps us write cleaner code. This method is executed first
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getHeader(AUTHORIZATION) == null || !request.getHeader(AUTHORIZATION).startsWith(TOKEN_PREFIX) ||
                request.getMethod().equalsIgnoreCase(HTTP_OPTIONS_METHOD) ||
                asList(PUBLIC_ROUTES).contains(request.getRequestURI());
    }

    private String getToken(HttpServletRequest request) {
//        return request.getHeader(AUTHORIZATION).substring(7);
        return ofNullable(request.getHeader(AUTHORIZATION))
                .filter(header -> header.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, EMPTY)).get(); // "Bearer " ==> 7 characters

    }

    private Map<String, String> getRequestValues(HttpServletRequest request) {
        return of(EMAIL_KEY, tokenProvider.getSubject(getToken(request), request), TOKEN_KEY, getToken(request));
    }
}
