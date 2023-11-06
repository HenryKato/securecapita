package io.hkfullstack.securecapita.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// In this class we set the rules for Authentication and Authorization
// In other words, we set the security rules for securing each of our endpoints
// We use HttpSecurity to ensure that each HttpRequest is safe for our application (passes this security configuration)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String[] PUBLIC_URLS = { "/users/login/**", "/users/verify/**" };
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().cors().disable();
        httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // don't manage session state because we are using token-based authentication
        httpSecurity.authorizeHttpRequests().requestMatchers(PUBLIC_URLS).permitAll(); // No need of authentication for these public urls
        httpSecurity.authorizeHttpRequests().requestMatchers(HttpMethod.DELETE, "/users/**").hasAnyAuthority("DELETE:USER"); // to delete a user, the user must be authorized
        httpSecurity.authorizeHttpRequests().requestMatchers(HttpMethod.DELETE, "customers/**").hasAnyAuthority("DELETE:CUSTOMER"); // to delete a customer, the user must be authorized
        httpSecurity.exceptionHandling().accessDeniedHandler(accessDeniedHandler).authenticationEntryPoint(authenticationEntryPoint);
        httpSecurity.authorizeHttpRequests().anyRequest().authenticated(); // all other requests must be authenticated

        return httpSecurity.build();
    }
}
