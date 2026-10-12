package com.tn.user.api;

import static com.tn.user.service.AvatarFixtures.contains;
import static com.tn.user.service.AvatarFixtures.decode;
import static com.tn.user.service.AvatarFixtures.jpeg;
import static com.tn.user.service.AvatarFixtures.jpegWithGps;
import static com.tn.user.service.AvatarFixtures.png;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Map;
import java.util.UUID;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tn.user.AbstractPostgresIntegrationTest;
import com.tn.user.domain.User;
import com.tn.user.domain.UserAvatar;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureTestRestTemplate
class AvatarIntegrationTest extends AbstractPostgresIntegrationTest
{
  @Autowired
  TestRestTemplate rest;

  @Autowired
  JdbcTemplate jdbc;

  @Autowired
  EntityManagerFactory entityManagerFactory;

  @Test
  void addingAnAvatar()
  {
    long id = newUser();

    ResponseEntity<User> response = put(id, jpeg(64, 64), "image/jpeg");

    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, response.getBody().avatarVersion());
    ResponseEntity<byte[]> avatar = rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id);
    assertEquals(200, avatar.getStatusCode().value());
    assertEquals(64, decode(avatar.getBody()).getWidth());
  }

  @Test
  void replacingAnAvatarChangesItsVersionAndKeepsOnlyTheNewPicture()
  {
    long id = newUser();
    put(id, jpeg(64, 64), "image/jpeg");

    ResponseEntity<User> response = put(id, png(32, 32), "image/png");

    assertEquals(2, response.getBody().avatarVersion());
    assertEquals(32, decode(rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id).getBody()).getWidth());
    assertEquals(1, avatarRows(id));
  }

  @Test
  void removingAnAvatar()
  {
    long id = newUser();
    put(id, jpeg(64, 64), "image/jpeg");

    ResponseEntity<Void> response = rest.exchange("/v1/users/{id}/avatar", DELETE, null, Void.class, id);

    assertEquals(204, response.getStatusCode().value());
    assertEquals(404, rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id).getStatusCode().value());
    assertNull(rest.getForEntity("/v1/users/{id}", User.class, id).getBody().avatarVersion());
    assertEquals(0, avatarRows(id));
  }

  @Test
  void removingWhenThereIsNoAvatarLeavesTheUserWithout()
  {
    long id = newUser();

    assertEquals(204, rest.exchange("/v1/users/{id}/avatar", DELETE, null, Void.class, id).getStatusCode().value());
  }

  @Test
  void aVersionIsNotReusedAfterTheAvatarIsRemoved()
  {
    long id = newUser();
    put(id, jpeg(64, 64), "image/jpeg");
    rest.exchange("/v1/users/{id}/avatar", DELETE, null, Void.class, id);

    ResponseEntity<User> response = put(id, jpeg(48, 48), "image/jpeg");

    assertEquals(2, response.getBody().avatarVersion());
  }

  @Test
  void versionsCountPerUser()
  {
    long first = newUser();
    long second = newUser();

    put(first, jpeg(32, 32), "image/jpeg");
    put(first, jpeg(32, 32), "image/jpeg");

    assertEquals(1, put(second, jpeg(32, 32), "image/jpeg").getBody().avatarVersion());
  }

  @Test
  void aPngSentAsAJpegIsServedAsAPng()
  {
    long id = newUser();
    put(id, png(32, 32), "image/jpeg");

    ResponseEntity<byte[]> response = rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id);

    assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
  }

  @Test
  void anAvatarIsNotCacheableByASharedCache()
  {
    long id = newUser();
    put(id, jpeg(32, 32), "image/jpeg");

    String cacheControl = rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id).getHeaders().getCacheControl();

    assertTrue(cacheControl.contains("private"), cacheControl);
    assertFalse(cacheControl.contains("public"), cacheControl);
  }

  @Test
  void locationIsNotKept()
  {
    long id = newUser();
    byte[] upload = jpegWithGps(64, 64);
    assertTrue(contains(upload, "Exif"));

    put(id, upload, "image/jpeg");

    assertFalse(contains(rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id).getBody(), "Exif"));
  }

  @Test
  void aFileThatIsNotAPictureIsRefusedAndTheAvatarIsUnchanged()
  {
    long id = newUser();
    put(id, jpeg(64, 64), "image/jpeg");
    byte[] kept = rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id).getBody();

    ResponseEntity<String> response = rest.exchange("/v1/users/{id}/avatar", PUT, entity("not a picture".getBytes(), "image/jpeg"), String.class, id);

    assertEquals(400, response.getStatusCode().value());
    assertArrayEquals(kept, rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id).getBody());
    assertEquals(1, rest.getForEntity("/v1/users/{id}", User.class, id).getBody().avatarVersion());
  }

  @Test
  void aPictureOver100KbIsRefused()
  {
    long id = newUser();

    ResponseEntity<String> response = rest.exchange("/v1/users/{id}/avatar", PUT, entity(new byte[102_401], "image/jpeg"), String.class, id);

    assertEquals(400, response.getStatusCode().value());
    assertEquals(0, avatarRows(id));
  }

  @Test
  void anUnknownUserIsNotFound()
  {
    assertEquals(404, rest.exchange("/v1/users/{id}/avatar", PUT, entity(jpeg(32, 32), "image/jpeg"), String.class, 999_999L).getStatusCode().value());
    assertEquals(404, rest.getForEntity("/v1/users/{id}/avatar", byte[].class, 999_999L).getStatusCode().value());
    assertEquals(404, rest.exchange("/v1/users/{id}/avatar", DELETE, null, Void.class, 999_999L).getStatusCode().value());
  }

  @Test
  void updatingAUserKeepsTheirAvatar()
  {
    long id = newUser();
    put(id, jpeg(32, 32), "image/jpeg");

    ResponseEntity<User> response = rest.exchange("/v1/users/{id}", PUT, new HttpEntity<>(Map.of("email", "renamed-" + UUID.randomUUID() + "@testing.com", "fullName", "New Name")), User.class, id);

    assertEquals(200, response.getStatusCode().value());
    assertEquals(1, response.getBody().avatarVersion());
    assertEquals(1, rest.getForEntity("/v1/users/{id}", User.class, id).getBody().avatarVersion());
  }

  @Test
  void deletingAUserDeletesTheirAvatar()
  {
    long id = newUser();
    put(id, jpeg(32, 32), "image/jpeg");

    rest.exchange("/v1/users/{id}", DELETE, null, Void.class, id);

    assertEquals(0, avatarRows(id));
  }

  @Test
  void readingUsersNeverReadsAPicture()
  {
    long id = newUser();
    put(id, jpeg(64, 64), "image/jpeg");
    SessionFactory sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    sessionFactory.getStatistics().clear();

    rest.getForEntity("/v1/users/{id}", User.class, id);
    rest.getForEntity("/v1/users", String.class);
    rest.exchange("/v1/users/{id}", PUT, new HttpEntity<>(Map.of("email", "again-" + UUID.randomUUID() + "@testing.com")), User.class, id);
    rest.postForEntity("/v1/actions/find-or-create", Map.of("email", "found-" + UUID.randomUUID() + "@testing.com"), User.class);

    assertEquals(0, sessionFactory.getStatistics().getEntityStatistics(UserAvatar.class.getName()).getLoadCount());
  }

  @Test
  void aPicturesContentsAreNotLogged()
  {
    Logger logger = (Logger)LoggerFactory.getLogger("com.tn");
    ListAppender<ILoggingEvent> appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);

    try
    {
      long id = newUser();
      put(id, jpeg(64, 64), "image/jpeg");
      rest.getForEntity("/v1/users/{id}/avatar", byte[].class, id);

      assertTrue(appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("Set avatar")));
      assertTrue(appender.list.stream().noneMatch(event -> event.getFormattedMessage().contains("JFIF") || event.getFormattedMessage().contains("ÿØ")));
      assertTrue(appender.list.stream().noneMatch(event -> event.getArgumentArray() != null && java.util.Arrays.stream(event.getArgumentArray()).anyMatch(argument -> argument instanceof byte[])));
    }
    finally
    {
      logger.detachAppender(appender);
    }
  }

  private long newUser()
  {
    ResponseEntity<User> response = rest.postForEntity("/v1/users", Map.of("email", UUID.randomUUID() + "@testing.com"), User.class);
    assertNotNull(response.getBody());

    return response.getBody().id();
  }

  private ResponseEntity<User> put(long id, byte[] image, String contentType)
  {
    return rest.exchange("/v1/users/{id}/avatar", PUT, entity(image, contentType), User.class, id);
  }

  private static HttpEntity<byte[]> entity(byte[] image, String contentType)
  {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType(contentType));

    return new HttpEntity<>(image, headers);
  }

  private int avatarRows(long id)
  {
    return jdbc.queryForObject("SELECT COUNT(*) FROM user_avatars WHERE user_id = ?", Integer.class, id);
  }
}
