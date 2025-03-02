package com.example.module_exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFeignClients
@SpringBootApplication
@EnableScheduling
public class ModuleExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleExchangeApplication.class, args);
	}

}
