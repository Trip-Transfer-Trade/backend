package com.example.module_exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.example.module_exchange", "com.example.module_utility"})
@EnableScheduling
public class ModuleExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleExchangeApplication.class, args);
	}

}
