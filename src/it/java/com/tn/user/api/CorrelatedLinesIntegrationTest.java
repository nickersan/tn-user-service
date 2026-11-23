package com.tn.user.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpMethod.PUT;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.JsonNode;

import com.tn.user.AbstractPostgresIntegrationTest;
import com.tn.user.JsonLines;
import com.tn.user.controller.AvatarMediaController;
import com.tn.user.domain.User;

/**
 * The lines the service logs for a request carry the action it is part of, who it is for, the app it came from and the service that
 * wrote them (okayat platform/logging, "An action that reaches a service"). The service calls no other service, so there is nothing
 * to send on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CorrelatedLinesIntegrationTest extends AbstractPostgresIntegrationTest
{
  private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
  private static final String TRACEPARENT = "00-" + TRACE_ID + "-00f067aa0ba902b7-01";
  private static final long MEDIA_ID = 123456789012345678L;
  private static final String CLIENT = "okayat-web/1.4.0 (web)";
  private static final String CLIENT_IN_BAGGAGE = "okayat-web/1.4.0%20(web)";

  @Autowired
  TestRestTemplate rest;

  private JsonLines jsonLines;

  @BeforeEach
  void captureLines()
  {
    jsonLines = JsonLines.capture();
  }

  @AfterEach
  void releaseLines()
  {
    jsonLines.release();
  }

  /** A member points their avatar at an item they uploaded, which okayat-bff asks this service to record. */
  @Test
  void shouldLogAnActionThatReachesAService() throws Exception
  {
    long id = newUser();
    String sessionId = UUID.randomUUID().toString();

    HttpHeaders headers = new HttpHeaders();
    headers.set("traceparent", TRACEPARENT);
    headers.set("baggage", "user.id=" + id + ",session.id=" + sessionId + ",client.name=" + CLIENT_IN_BAGGAGE);

    ResponseEntity<User> response = rest.exchange("/v1/users/{id}/avatar-media/{mediaId}", PUT, new HttpEntity<>(headers), User.class, id, MEDIA_ID);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    List<JsonNode> traced = jsonLines.requests(2).stream().filter(line -> TRACE_ID.equals(line.path("traceId").asString())).toList();
    assertEquals(1, traced.size(), traced.toString());
    JsonNode request = traced.getFirst();
    assertEquals("/v1/users/" + id + "/avatar-media/" + MEDIA_ID, request.path("path").asString(), request.toString());
    assertCorrelated(request, id, sessionId);

    List<JsonNode> set = jsonLines.from(AvatarMediaController.class.getName());
    assertEquals(1, set.size(), set.toString());
    assertEquals("Set avatar media", set.getFirst().path("message").asString(), set.toString());
    assertCorrelated(set.getFirst(), id, sessionId);
  }

  private void assertCorrelated(JsonNode line, long userId, String sessionId)
  {
    assertEquals(TRACE_ID, line.path("traceId").asString(), line.toString());
    assertEquals(String.valueOf(userId), line.path("user.id").asString(), line.toString());
    assertEquals(sessionId, line.path("session.id").asString(), line.toString());
    assertEquals(CLIENT, line.path("client.name").asString(), line.toString());
    assertEquals("tn-user-service", line.path("service.name").asString(), line.toString());
    assertFalse(line.path("service.version").asString().isBlank(), line.toString());
  }

  private long newUser()
  {
    ResponseEntity<User> response = rest.postForEntity("/v1/users", Map.of("email", UUID.randomUUID() + "@testing.com"), User.class);
    assertNotNull(response.getBody());

    return response.getBody().id();
  }
}
