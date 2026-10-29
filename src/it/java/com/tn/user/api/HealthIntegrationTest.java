package com.tn.user.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

import com.tn.user.AbstractPostgresIntegrationTest;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
class HealthIntegrationTest extends AbstractPostgresIntegrationTest
{
  @Autowired
  TestRestTemplate testRestTemplate;

  @Autowired
  Environment environment;

  @Test
  void shouldBeReadyWithItsDatasource()
  {
    ResponseEntity<String> response = testRestTemplate.getForEntity("/actuator/health/readiness", String.class);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("{\"status\":\"UP\"}", response.getBody());
  }

  @Test
  void shouldBeLive()
  {
    ResponseEntity<String> response = testRestTemplate.getForEntity("/actuator/health/liveness", String.class);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("{\"status\":\"UP\"}", response.getBody());
  }

  @Test
  void shouldHaveNoResilienceEndpointAsItDoesNotUseTnResilience()
  {
    assertEquals(404, testRestTemplate.getForEntity("/actuator/resilience", String.class).getStatusCode().value());
  }

  @Test
  void shouldExposeOnlyHealthAndResilience()
  {
    assertEquals("health,resilience", environment.getProperty("management.endpoints.web.exposure.include"));
    assertEquals(404, testRestTemplate.getForEntity("/actuator/env", String.class).getStatusCode().value());
    assertEquals(404, testRestTemplate.getForEntity("/actuator/heapdump", String.class).getStatusCode().value());
    assertEquals(404, testRestTemplate.getForEntity("/actuator/beans", String.class).getStatusCode().value());
    assertEquals(404, testRestTemplate.getForEntity("/actuator/loggers", String.class).getStatusCode().value());
  }

  @Test
  void shouldGetItsHealthDefaultsFromTheTnServiceItDependsOn()
  {
    assertEquals("true", environment.getProperty("management.endpoint.health.probes.enabled"));
    assertEquals("readinessState,db", environment.getProperty("management.endpoint.health.group.readiness.include"));
  }
}
