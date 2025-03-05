package com.example.module_trip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class, scanBasePackages = {"com.example.module_trip", "com.example.module_utility"})
public class ModuleTripApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleTripApplication.class, args);
	}

}
