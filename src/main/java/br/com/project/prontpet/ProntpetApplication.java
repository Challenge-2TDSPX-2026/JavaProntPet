package br.com.project.prontpet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProntpetApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProntpetApplication.class, args);
	}

}
