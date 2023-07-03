package com.example.naejango;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NaejangoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaejangoApplication.class, args);
		System.out.println(org.hibernate.Version.getVersionString());
	}

}
