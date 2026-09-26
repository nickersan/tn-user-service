package com.tn.user.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static com.tn.user.domain.IdentifierType.EMAIL;
import static com.tn.user.domain.IdentifierType.PHONE;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Isolated;
import org.opentest4j.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;

import com.tn.lang.Iterables;
import com.tn.user.AbstractPostgresIntegrationTest;
import com.tn.user.domain.User;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserRepositoryIntegrationTest extends AbstractPostgresIntegrationTest
{
  private static final User USER = new User("test.tester@testing.com", null, "Test Tester", "Test");

  @Autowired
  private UserRepository userRepository;

  private void assertUser(User expected, User actual)
  {
    assertEquals(expected.email(), actual.email());
    assertEquals(expected.phone(), actual.phone());
    assertEquals(expected.fullName(), actual.fullName());
    assertEquals(expected.preferredName(), actual.preferredName());
  }

  @Nested
  @Isolated
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
  class CrudTest
  {
    private static final AtomicReference<Long> USER_ID = new AtomicReference<>();

    @Test
    @Order(1)
    @Rollback(false)
    void shouldSave()
    {
      User user = userRepository.save(USER);
      assertUser(USER, user);
      assertNotNull(user.id());
//    assertNotNull(user.created());

      USER_ID.set(user.id());
    }

    @Test
    @Order(2)
    void shouldRead()
    {
      User user = StreamSupport.stream(userRepository.findAll().spliterator(), false).findFirst().orElseThrow(AssertionFailedError::new);
      assertUser(USER, user);
      assertNotNull(user.created());
    }

    @Test
    @Order(3)
    void shouldReadById()
    {
      User user = userRepository.findById(USER_ID.get()).orElseThrow(AssertionFailedError::new);
      assertUser(USER, user);
      assertNotNull(user.created());
    }

    @Test
    @Order(4)
    void shouldDelete()
    {
      User user = userRepository.findById(USER_ID.get()).orElseThrow(AssertionFailedError::new);
      userRepository.delete(user);
      assertTrue(userRepository.findById(USER_ID.get()).isEmpty());
    }
  }

  @Nested
  @Isolated
  @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
  class QueryTest
  {
    private static final User USER_2 = new User("another.test@testing.com", null, "Another Test", "Another");

    @BeforeEach
    void createUsers()
    {
      userRepository.saveAll(List.of(copy(USER), copy(USER_2)));
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
    void shouldFindByFullName()
    {
      assertWhere(expectedUser -> "fullName = " + expectedUser.fullName());
    }

    @Test
    void shouldFindByPreferredName()
    {
      assertWhere(expectedUser -> "preferredName = " + expectedUser.preferredName());
    }

    private void assertWhere(Function<User, String> queryProvider)
    {
      List<User> users = Iterables.asList(userRepository.findWhere(queryProvider.apply(USER_2)));
      assertEquals(1, users.size());
      assertUser(USER_2, users.getFirst());
    }

    private User copy(User user)
    {
      return new User(null, user.email(), user.phone(), user.fullName(), user.preferredName(), null);
    }
  }

  @Nested
  @Isolated
  @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
  class FindOrCreateTest
  {
    @AfterEach
    void deleteUsers()
    {
      userRepository.deleteAll();
    }

    @Test
    void shouldCreateANewUserPerIdentifierType()
    {
      User email = userRepository.findOrCreate(EMAIL, "find-or-create.email@testing.com");
      assertEquals("find-or-create.email@testing.com", email.email());

      User phone = userRepository.findOrCreate(PHONE, "+441234500000");
      assertEquals("+441234500000", phone.phone());
    }

    @Test
    void shouldReturnTheExistingUserForAKnownValue()
    {
      User created = userRepository.findOrCreate(EMAIL, "find-or-create.existing@testing.com");
      User found = userRepository.findOrCreate(EMAIL, "find-or-create.existing@testing.com");

      assertEquals(created.id(), found.id());
    }

    @Test
    void shouldRejectCreatingADuplicateEmailDirectly()
    {
      userRepository.save(new User("duplicate.email@testing.com", null, null, null));

      assertThrows(DataIntegrityViolationException.class, () -> userRepository.save(new User("duplicate.email@testing.com", null, null, null)));
    }
  }
}
