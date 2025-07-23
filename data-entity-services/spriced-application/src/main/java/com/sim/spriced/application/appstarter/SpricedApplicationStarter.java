package com.sim.spriced.application.appstarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication(scanBasePackages = {"com.sim.spriced.platform", "com.sim.spriced.application","com.sim.spriced.custom"})
public class SpricedApplicationStarter {
	public static void main(String[] args) {

		SpringApplication.run(SpricedApplicationStarter.class, args);

	}
}