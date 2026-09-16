package com.tn.user;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractPostgresIntegrationTest
{
  @Container
  @ServiceConnection("postgresql")
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");
}
