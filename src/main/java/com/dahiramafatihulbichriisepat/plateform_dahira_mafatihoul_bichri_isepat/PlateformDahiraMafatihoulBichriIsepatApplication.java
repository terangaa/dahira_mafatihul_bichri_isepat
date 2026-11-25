package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class PlateformDahiraMafatihoulBichriIsepatApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlateformDahiraMafatihoulBichriIsepatApplication.class, args);
	}

	// =========================================================
	// CONFIGURATION CORS (AUTORISE TON FRONTEND À COMMUNIQUER)
	// =========================================================
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOriginPatterns("*") // Autorise toutes les origines
						.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // ✅ TOUTES les méthodes
						.allowedHeaders("*")
						.exposedHeaders("Authorization")
						.allowCredentials(true)
						.maxAge(3600); // Cache la réponse preflight pendant 1h
			}
		};
	}
}