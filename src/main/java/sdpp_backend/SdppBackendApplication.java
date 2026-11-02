package sdpp_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// A autenticação é feita por JWT (JwtAuthFilter); o usuário padrão em memória do Spring não é usado.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class SdppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SdppBackendApplication.class, args);
	}

}
