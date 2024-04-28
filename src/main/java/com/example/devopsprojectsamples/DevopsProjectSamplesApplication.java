package com.example.devopsprojectsamples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class DevopsProjectSamplesApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevopsProjectSamplesApplication.class, args);
	}

	@GetMapping
	public String sayHello(){ return "Hello from devops project !";}

}
