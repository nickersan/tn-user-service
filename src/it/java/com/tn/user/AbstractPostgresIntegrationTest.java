package com.tn.user;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Deliberately not using {@code @Testcontainers}/{@code @Container}: those stop the
 * container after the first test class that used it finishes, breaking every other
 * class that shares this base in the same (Surefire-reused) JVM. This is
 * Testcontainers' documented "singleton container" pattern instead - start once,
 * never stop; Ryuk reaps it when the JVM exits.
 */
public abstract class AbstractPostgresIntegrationTest
{
  @ServiceConnection("postgresql")
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

  static
  {
    postgres.start();
  }
}
