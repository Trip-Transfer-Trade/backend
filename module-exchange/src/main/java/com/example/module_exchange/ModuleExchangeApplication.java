package com.example.module_exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class ModuleExchangeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModuleExchangeApplication.class, args);
	}

}
