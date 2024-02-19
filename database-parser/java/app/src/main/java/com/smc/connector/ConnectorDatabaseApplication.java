package com.smc.connector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ConnectorDatabaseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectorGitlabApplication.class, args);
	}

}
