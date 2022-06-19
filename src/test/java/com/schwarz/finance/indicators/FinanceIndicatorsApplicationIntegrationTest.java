package com.schwarz.finance.indicators;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FinanceIndicatorsApplicationIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    public void getHealthIT() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
        assertThat(response.getBody()).isEqualTo("It's all right!");
    }



}
