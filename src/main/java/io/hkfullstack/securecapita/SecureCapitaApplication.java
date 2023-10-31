package io.hkfullstack.securecapita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class SecureCapitaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureCapitaApplication.class, args);
	}

}
