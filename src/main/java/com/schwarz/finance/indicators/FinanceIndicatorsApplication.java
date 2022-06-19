package com.schwarz.finance.indicators;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanceIndicatorsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceIndicatorsApplication.class, args);
	}


	/*
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {

		return args -> {
			System.out.println("Let's print out all the Beans provides by SpringBoot!");

			String[] beanNames = ctx.getBeanDefinitionNames();
			Arrays.sort(beanNames);
			for (String beanName : beanNames) {
				System.out.println(beanName);
			}
		};

	}
	*/

}
