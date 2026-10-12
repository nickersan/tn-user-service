package com.tn.user.api;

import static com.tn.user.service.AvatarFixtures.jpeg;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.tn.user.AbstractPostgresIntegrationTest;
import com.tn.user.domain.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "tn.user-service.avatar.upload-limit=2")
@AutoConfigureTestRestTemplate
class AvatarUploadLimitIntegrationTest extends AbstractPostgresIntegrationTest
{
  @Autowired
  TestRestTemplate rest;

  @Test
  void uploadingTooOftenIsRefusedAndLeavesTheAvatarAsItWas()
  {
    long id = rest.postForEntity("/v1/users", Map.of("email", UUID.randomUUID() + "@testing.com"), User.class).getBody().id();

    assertEquals(200, put(id, jpeg(32, 32)).getStatusCode().value());
    assertEquals(200, put(id, jpeg(40, 40)).getStatusCode().value());

    ResponseEntity<String> refused = put(id, jpeg(48, 48));

    assertEquals(429, refused.getStatusCode().value());
    assertEquals(2, rest.getForEntity("/v1/users/{id}", User.class, id).getBody().avatarVersion());
    assertEquals(40, decodedWidth(id));
  }

  @Test
  void failedUploadsCountAgainstTheLimit()
  {
    long id = rest.postForEntity("/v1/users", Map.of("email", UUID.randomUUID() + "@testing.com"), User.class).getBody().id();

    assertEquals(400, put(id, "not a picture".getBytes()).getStatusCode().value());
    assertEquals(400, put(id, "not a picture".getBytes()).getStatusCode().value());

    assertEquals(429, put(id, jpeg(32, 32)).getStatusCode().value());
  }

  @Test
  void theLimitIsPerUser()
  {
    long first = rest.postForEntity("/v1/users", Map.of("email", UUID.randomUUID() + "@testing.com"), User.class).getBody().id();
    long second = rest.postForEntity("/v1/users", Map.of("email", UUID.randomUUID() + "@testing.com"), User.class).getBody().id();
    put(first, jpeg(32, 32));
    put(first, jpeg(32, 32));

    assertEquals(200, put(second, jpeg(32, 32)).getStatusCode().value());
  }

  private ResponseEntity<String> put(long id, byte[] image)
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);

    return rest.exchange("/v1/users/{id}/avatar", PUT, new HttpEntity<>(image, headers), String.class, id);
  }

  private int decodedWidth(long id)
  {
    return com.tn.user.service.AvatarFixtures.decode(rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id).getBody()).getWidth();
  }
}
