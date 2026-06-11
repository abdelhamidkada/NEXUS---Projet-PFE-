package com.dyxia.nexuserp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NexusErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexusErpApplication.class, args);
	}

}
