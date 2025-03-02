package com.example.ttt_root_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@ComponentScan(basePackages = {
		"com.example.module_member",
		"com.example.module_utility",
		"com.example.module_exchange",
		"com.example.module_stock",
		"com.example.module_trip",
		"com.example.gateway_service",
		"com.example.module_alarm"
})
public class TttRootProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(TttRootProjectApplication.class, args);
	}
}
