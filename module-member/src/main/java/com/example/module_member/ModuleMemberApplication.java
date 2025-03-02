package com.example.module_member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
		"com.example.module_member",
		"com.example.module_utility"
})
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class ModuleMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleMemberApplication.class, args);
	}

}
