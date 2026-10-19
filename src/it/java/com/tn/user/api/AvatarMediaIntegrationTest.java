package com.tn.user.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import com.tn.user.AbstractPostgresIntegrationTest;
import com.tn.user.domain.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class AvatarMediaIntegrationTest extends AbstractPostgresIntegrationTest
{
  @Autowired
  TestRestTemplate rest;

  @Test
  void pointingAnAvatarAtAnItem()
  {
    long id = newUser();

    ResponseEntity<User> response = rest.exchange("/v1/users/{id}/avatar-media/{mediaId}", PUT, null, User.class, id, 123456789012345678L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(123456789012345678L, response.getBody().avatarMediaId());
    assertEquals(123456789012345678L, rest.getForObject("/v1/users/{id}", User.class, id).avatarMediaId());
  }

  @Test
  void pointingAtAnotherItemReplacesTheFirst()
  {
    long id = newUser();
    rest.exchange("/v1/users/{id}/avatar-media/{mediaId}", PUT, null, User.class, id, 1L);

    ResponseEntity<User> response = rest.exchange("/v1/users/{id}/avatar-media/{mediaId}", PUT, null, User.class, id, 2L);

    assertEquals(2L, response.getBody().avatarMediaId());
  }

  @Test
  void clearingTheAvatar()
  {
    long id = newUser();
    rest.exchange("/v1/users/{id}/avatar-media/{mediaId}", PUT, null, User.class, id, 1L);

    ResponseEntity<User> response = rest.exchange("/v1/users/{id}/avatar-media", DELETE, null, User.class, id);

    assertEquals(200, response.getStatusCode().value());
    assertNull(response.getBody().avatarMediaId());
    assertNull(rest.getForObject("/v1/users/{id}", User.class, id).avatarMediaId());
  }

  @Test
  void clearingWhenThereIsNoneIsNotAnError()
  {
    assertEquals(200, rest.exchange("/v1/users/{id}/avatar-media", DELETE, null, User.class, newUser()).getStatusCode().value());
  }

  @Test
  void updatingAUserKeepsTheirAvatarMedia()
  {
    long id = newUser();
    rest.exchange("/v1/users/{id}/avatar-media/{mediaId}", PUT, null, User.class, id, 5L);

    rest.exchange("/v1/users/{id}", PUT, new HttpEntity<>(Map.of("email", UUID.randomUUID() + "@testing.com", "fullName", "New Name")), User.class, id);

    assertEquals(5L, rest.getForObject("/v1/users/{id}", User.class, id).avatarMediaId());
  }

  @Test
  void anUnknownUserIsNotFound()
  {
    assertEquals(404, rest.exchange("/v1/users/{id}/avatar-media/{mediaId}", PUT, null, String.class, 987654321L, 1L).getStatusCode().value());
    assertEquals(404, rest.exchange("/v1/users/{id}/avatar-media", DELETE, null, String.class, 987654321L).getStatusCode().value());
  }

  private long newUser()
  {
    ResponseEntity<User> response = rest.postForEntity("/v1/users", Map.of("email", UUID.randomUUID() + "@testing.com"), User.class);
    assertNotNull(response.getBody());

    return response.getBody().id();
  }
}
