package com.tn.user.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tn.user.AbstractPostgresIntegrationTest;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class UserControllerIntegrationTest extends AbstractPostgresIntegrationTest
{
  private static final long USER_ID = 1L;
  private static final User USER = new User(USER_ID, "test.tester@testing.com", null, "Test", "Tester", LocalDateTime.now());

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  TestRestTemplate testRestTemplate;

  @Test
  void shouldGetUser()
  {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(USER));

    ResponseEntity<User> response = testRestTemplate.getForEntity("/v1/users/{id}", User.class, USER_ID);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(USER, response.getBody());
  }

  @Test
  void shouldDeleteUser()
  {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(USER));

    ResponseEntity<Void> response = testRestTemplate.exchange("/v1/users/{id}", DELETE, null, Void.class, USER_ID);

    assertEquals(204, response.getStatusCode().value());

    verify(userRepository).delete(USER);
  }
}
