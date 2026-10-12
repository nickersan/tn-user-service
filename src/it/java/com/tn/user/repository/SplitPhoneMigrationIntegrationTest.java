package com.tn.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;

import com.tn.user.AbstractPostgresIntegrationTest;

/**
 * Runs the phone-splitting migration against rows written by the schema before it, each test in a
 * schema of its own.
 */
class SplitPhoneMigrationIntegrationTest extends AbstractPostgresIntegrationTest
{
  private static final AtomicInteger SCHEMA_COUNTER = new AtomicInteger();

  @Test
  void shouldSplitAValueWithoutAPlus() throws Exception
  {
    String schema = migrateTo("1.0.00");
    insertUser(schema, 1, "447911123456");

    migrateToLatest(schema);

    assertEquals("44:7911123456", phoneOf(schema, 1));
  }

  @Test
  void shouldSplitAValueWithAPlus() throws Exception
  {
    String schema = migrateTo("1.0.00");
    insertUser(schema, 1, "+447911123456");

    migrateToLatest(schema);

    assertEquals("44:7911123456", phoneOf(schema, 1));
  }

  @Test
  void shouldLeaveAUserWithoutAPhoneWithout() throws Exception
  {
    String schema = migrateTo("1.0.00");
    insertUser(schema, 1, null);

    migrateToLatest(schema);

    assertEquals(null, phoneOf(schema, 1));
  }

  @Test
  void shouldFailListingBothUsersWhoHoldTheSameNumberWrittenTwoWays() throws Exception
  {
    String schema = migrateTo("1.0.00");
    insertUser(schema, 1001, "+447911123456");
    insertUser(schema, 1002, "447911123456");

    FlywayException exception = assertThrows(FlywayException.class, () -> migrateToLatest(schema));

    assertTrue(allMessages(exception).contains("[[1001, 1002]]"), allMessages(exception));
  }

  @Test
  void shouldFailListingAnUnparsableValue() throws Exception
  {
    String schema = migrateTo("1.0.00");
    insertUser(schema, 2001, "+4477");

    FlywayException exception = assertThrows(FlywayException.class, () -> migrateToLatest(schema));

    assertTrue(allMessages(exception).contains("[2001]"), allMessages(exception));
  }

  private String migrateTo(String version) throws Exception
  {
    String schema = "split_phone_" + SCHEMA_COUNTER.incrementAndGet();
    flyway(schema).target(version).load().migrate();

    return schema;
  }

  private void migrateToLatest(String schema)
  {
    flyway(schema).load().migrate();
  }

  private org.flywaydb.core.api.configuration.FluentConfiguration flyway(String schema)
  {
    return Flyway.configure()
      .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
      .schemas(schema)
      .createSchemas(true);
  }

  private void insertUser(String schema, long userId, String phone) throws Exception
  {
    try (Connection connection = connection(); Statement statement = connection.createStatement())
    {
      statement.execute("INSERT INTO " + schema + ".users (user_id, phone) VALUES (" + userId + ", " + (phone == null ? "NULL" : "'" + phone + "'") + ")");
    }
  }

  private String phoneOf(String schema, long userId) throws Exception
  {
    try (Connection connection = connection(); Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(
      "SELECT phone_country_code, phone_number FROM " + schema + ".users WHERE user_id = " + userId))
    {
      rows.next();
      String number = rows.getString("phone_number");

      return number == null ? null : rows.getInt("phone_country_code") + ":" + number;
    }
  }

  private Connection connection() throws Exception
  {
    return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
  }

  private String allMessages(Throwable throwable)
  {
    StringBuilder messages = new StringBuilder();
    for (Throwable cause = throwable; cause != null; cause = cause.getCause()) messages.append(cause.getMessage()).append('\n');

    return messages.toString();
  }
}
