package com.synapse.knowledge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.modulith.Modulithic;

@EnableJpaAuditing
@Modulithic
@SpringBootApplication
public class KnowledgeSvcApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowledgeSvcApplication.class, args);
	}

}
