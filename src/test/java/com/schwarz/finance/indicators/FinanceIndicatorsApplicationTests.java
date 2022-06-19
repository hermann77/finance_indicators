package com.schwarz.finance.indicators;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.reactive.server.WebTestClient; // tests for Netty/WebFlux


// @SpringBootTest
// @AutoConfigureMockMvc // tomcat
@AutoConfigureWebTestClient // netty/webflux
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // netty/webflux
class FinanceIndicatorsApplicationTests {

	@Autowired
	// private MockMvc mockMvc;
	private WebTestClient webClient; // netty/webflux

	@Test
	public void getHealth() throws Exception {
/*
		Tests for Tomcat
		mockMvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string("It's all right!"));
*/
		// tests for netty/webflux
		webClient.get().uri("/").exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("It's all right!");
	}

	@Test
	void contextLoads() {
	}

}
