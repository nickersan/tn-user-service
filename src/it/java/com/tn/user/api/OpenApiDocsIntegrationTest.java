package com.tn.user.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import com.tn.user.AbstractPostgresIntegrationTest;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestRestTemplate
class OpenApiDocsIntegrationTest extends AbstractPostgresIntegrationTest
{
  @Autowired
  TestRestTemplate testRestTemplate;

  @Test
  void shouldDescribeUserEndpoints()
  {
    String docs = testRestTemplate.getForObject("/v3/api-docs", String.class);

    assertTrue(docs.contains("/v1/users"), "api-docs should describe /v1/users");
    assertTrue(docs.contains("/v1/actions/find-or-create"), "api-docs should describe /v1/actions/find-or-create");
  }
}
