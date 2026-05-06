package com.tunhan.micsu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.tunhan.micsu.entity"})
@EnableJpaRepositories(basePackages = {"com.tunhan.micsu.repository"})
@EnableJpaAuditing
public class MicsuApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicsuApplication.class, args);
	}

}
