package io.hkfullstack.securecapita;

import org.apache.catalina.filters.CorsFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class SecureCapitaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureCapitaApplication.class, args);
	}

//	@Bean
//	public CorsFilter corsFilter() {
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		CorsConfiguration corsConfiguration = new CorsConfiguration();
//		corsConfiguration.setAllowCredentials(true);
//		corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));
//		corsConfiguration.setAllowedHeaders(Arrays.asList("origin", "Access-Control-Allow-Origin"));
//		return new CorsFilter(source);
//	}
}
