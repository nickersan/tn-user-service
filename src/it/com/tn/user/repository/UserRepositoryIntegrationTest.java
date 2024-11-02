package com.tn.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import com.tn.user.domain.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserRepositoryIntegrationTest
{
  private static final User USER = User.builder()
    .email("test.tester@testing.com")
    .firstName("Test")
    .lastName("Tester")
    .tokenSubject("TK1")
    .build();

  @Autowired
  private UserRepository userRepository;

  @Test
  @Order(1)
  @Rollback(false)
  void shouldSave()
  {
    var user = userRepository.save(USER);
    assertUser(USER, user);
    assertNotNull(user.id());
//    assertNotNull(user.created());
  }

  @Test
  @Order(2)
  void shouldRead()
  {
    var user = StreamSupport.stream(userRepository.findAll().spliterator(), false).findFirst().orElseThrow(AssertionFailedError::new);
    assertUser(USER, user);
    assertNotNull(user.created());
  }

  @Test
  @Order(3)
  void shouldReadById()
  {
    var user = userRepository.findById(1L).orElseThrow(AssertionFailedError::new);
    assertUser(USER, user);
    assertNotNull(user.created());
  }

  @Test
  @Order(4)
  void shouldDelete()
  {
    var user = userRepository.findById(1L).orElseThrow(AssertionFailedError::new);
    userRepository.delete(user);
    assertTrue(userRepository.findById(1L).isEmpty());
  }

  private static void assertUser(User expected, User actual)
  {
    assertEquals(expected.email(), actual.email());
    assertEquals(expected.firstName(), actual.firstName());
    assertEquals(expected.lastName(), actual.lastName());
    assertEquals(expected.tokenSubject(), actual.tokenSubject());
  }

  @Nested
  class QueryTest
  {
    private static final User USER_2 = User.builder()
      .email("another.test@testing.com")
      .firstName("Another")
      .lastName("Test")
      .tokenSubject("TK2")
      .build();

    @BeforeEach
    void createUsers()
    {
      userRepository.saveAll(List.of(USER, USER_2));
    }

    @AfterEach
    void deleteUsers()
    {
      userRepository.deleteAll();
    }

    @Test
    void shouldFindByEmail()
    {
      assertWhere(expectedUser -> "email = " + expectedUser.email());
    }

    @Test
    void shouldFindByFirstName()
    {
      assertWhere(expectedUser -> "firstName = " + expectedUser.firstName());
    }

    @Test
    void shouldFindByLastName()
    {
      assertWhere(expectedUser -> "lastName = " + expectedUser.lastName());
    }

    @Test
    void shouldFindByTokenSubject()
    {
      assertWhere(expectedUser -> "tokenSubject = " + expectedUser.tokenSubject());

    }

    private void assertWhere(Function<User, String> queryProvider)
    {
      var users = StreamSupport.stream(userRepository.findWhere(queryProvider.apply(USER_2)).spliterator(), false).toList();
      assertEquals(1, users.size());
      assertUser(USER_2, users.getFirst());
    }
  }
}
