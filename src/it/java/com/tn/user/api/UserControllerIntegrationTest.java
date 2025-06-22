package com.tn.user.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest
{
  private static final long USER_ID = 1L;
  private static final User USER = new User(USER_ID, "test.tester@testing.com", "Test", "Tester", "T1", LocalDateTime.now());

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  TestRestTemplate testRestTemplate;

  @Test
  void shouldDeleteUser()
  {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(USER));

    ResponseEntity<User> response = testRestTemplate.exchange("/v1/{userId}", DELETE, null, User.class, USER_ID);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(USER, response.getBody());

    verify(userRepository).delete(USER);
  }
}
