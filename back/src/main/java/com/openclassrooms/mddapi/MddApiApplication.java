package com.openclassrooms.mddapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class MddApiApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = run(args);
		if (Boolean.getBoolean("mdd.exitAfterStartup")) {
			ctx.close();
		}
	}

	static ConfigurableApplicationContext run(String... args) {
		return SpringApplication.run(MddApiApplication.class, args);
	}

}
