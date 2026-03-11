package com.project.nyamtori;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NyamtoriApplication {

	public static void main(String[] args) {
		SpringApplication.run(NyamtoriApplication.class, args);
	}

}
