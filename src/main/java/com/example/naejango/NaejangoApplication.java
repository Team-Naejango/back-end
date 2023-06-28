package com.example.naejango;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class NaejangoApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaejangoApplication.class, args);
		System.out.println(org.hibernate.Version.getVersionString());
	}

}
