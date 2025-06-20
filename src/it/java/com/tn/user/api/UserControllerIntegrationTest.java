package com.tn.user.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest
{
  private static final long USER_ID = 1L;
  private static final User UNSAVED_USER = new User("test.tester@testing.com", "Test", "Tester", "T1");
  private static final User USER = new User(USER_ID, "test.tester@testing.com", "Test", "Tester", "T1", LocalDateTime.now());
  private static final ParameterizedTypeReference<List<User>> USER_LIST = new ParameterizedTypeReference<>() {};

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  TestRestTemplate testRestTemplate;

  @Test
  void shouldReturnAllUsers()
  {
    when(userRepository.findAll(Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1", GET, null, USER_LIST);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForIdWithPath()
  {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(USER));

    ResponseEntity<User> response = testRestTemplate.exchange("/v1/{userId}", GET, null, User.class, USER_ID);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(USER, response.getBody());
  }

  @Test
  void shouldReturnNotFoundForIdWithPathWhenNotFound()
  {
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    ResponseEntity<User> response = testRestTemplate.exchange("/v1/{userId}", GET, null, User.class, USER_ID);

    assertTrue(response.getStatusCode().is4xxClientError());
  }

  @Test
  void shouldReturnUserForIdWithParam()
  {
    when(userRepository.findAllById(Set.of(USER.id()))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?id={userId}", GET, null, USER_LIST, USER.id());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForIdWithQuery()
  {
    when(userRepository.findWhere("id=" + USER.id(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?q=id={userId}", GET, null, USER_LIST, USER.id());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForEmailWithParam()
  {
    when(userRepository.findWhere("email=" + USER.email(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?email={userEmail}", GET, null, USER_LIST, USER.email());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForEmailWithQuery()
  {
    when(userRepository.findWhere("email=" + USER.email(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?q=email={userEmail}", GET, null, USER_LIST, USER.email());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForFirstNameWithParam()
  {
    when(userRepository.findWhere("firstName=" + USER.firstName(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?firstName={userFirstName}", GET, null, USER_LIST, USER.firstName());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForFirstNameWithQuery()
  {
    when(userRepository.findWhere("firstName=" + USER.firstName(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?q=firstName={userFirstName}", GET, null, USER_LIST, USER.firstName());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForLastNameWithParam()
  {
    when(userRepository.findWhere("lastName=" + USER.lastName(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?lastName={userLastName}", GET, null, USER_LIST, USER.lastName());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForLastNameWithQuery()
  {
    when(userRepository.findWhere("lastName=" + USER.lastName(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?q=lastName={userLastName}", GET, null, USER_LIST, USER.lastName());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForTokenSubjectWithParam()
  {
    when(userRepository.findWhere("tokenSubject=" + USER.tokenSubject(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?tokenSubject={userTokenSubject}", GET, null, USER_LIST, USER.tokenSubject());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForTokenSubjectWithQuery()
  {
    when(userRepository.findWhere("tokenSubject=" + USER.tokenSubject(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?q=tokenSubject={userTokenSubject}", GET, null, USER_LIST, USER.tokenSubject());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForCreatedWithParam()
  {
    when(userRepository.findWhere("created=" + USER.created(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?created={userCreated}", GET, null, USER_LIST, USER.created());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnUserForCreatedWithQuery()
  {
    when(userRepository.findWhere("created=" + USER.created(), Sort.by(Sort.Direction.ASC, User.Fields.id))).thenReturn(List.of(USER));

    ResponseEntity<List<User>> response = testRestTemplate.exchange("/v1?q=created={userCreated}", GET, null, USER_LIST, USER.created());

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(List.of(USER), response.getBody());
  }

  @Test
  void shouldReturnBadRequestForGetWithInvalidParam()
  {
    ResponseEntity<Void> response = testRestTemplate.exchange("/v1?invalid=X", GET, null, Void.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldReturnBadRequestForGetWithInvalidQuery()
  {
    ResponseEntity<Void> response = testRestTemplate.exchange("/v1?q=invalid=X", GET, null, Void.class);

    assertEquals(BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void shouldReturnInternalServerErrorForGetOnUncaughtException()
  {
    when(userRepository.findWhere("email=X", Sort.by(Sort.Direction.ASC, User.Fields.id))).thenThrow(new RuntimeException());

    ResponseEntity<Void> response = testRestTemplate.exchange("/v1?email=X", GET, null, Void.class);

    assertEquals(INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void shouldSaveUserWithPost()
  {
    when(userRepository.save(UNSAVED_USER)).thenReturn(USER);

    ResponseEntity<User> response = testRestTemplate.exchange("/v1", POST, new HttpEntity<>(UNSAVED_USER), User.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertEquals(USER, response.getBody());
  }

  @Test
  void shouldBadRequestForSaveUserWithPostAndMissingEmail()
  {
    ResponseEntity<User> response = testRestTemplate.exchange("/v1", POST, new HttpEntity<>(new User(null, "Test", "Tester", "T1")), User.class);

    assertTrue(response.getStatusCode().is4xxClientError());
  }

  @Test
  void shouldBadRequestForSaveUserWithPostAndCorruptEmail()
  {
    ResponseEntity<User> response = testRestTemplate.exchange("/v1", POST, new HttpEntity<>(new User("CORRUPT", "Test", "Tester", "T1")), User.class);

    assertTrue(response.getStatusCode().is4xxClientError());
  }

  @Test
  void shouldBadRequestForSaveUserWithPostAndMissingFirstName()
  {
    ResponseEntity<User> response = testRestTemplate.exchange("/v1", POST, new HttpEntity<>(new User("test.tester@testing.com", null, "Tester", "T1")), User.class);

    assertTrue(response.getStatusCode().is4xxClientError());
  }

  @Test
  void shouldBadRequestForSaveUserWithPostAndMissingLastName()
  {
    ResponseEntity<User> response = testRestTemplate.exchange("/v1", POST, new HttpEntity<>(new User("test.tester@testing.com", "Test", null, "T1")), User.class);

    assertTrue(response.getStatusCode().is4xxClientError());
  }

  @Test
  void shouldSaveUserWithPut()
  {
    when(userRepository.save(UNSAVED_USER)).thenReturn(USER);

    ResponseEntity<User> response = testRestTemplate.exchange("/v1", PUT, new HttpEntity<>(UNSAVED_USER), User.class, USER_ID);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(USER, response.getBody());
  }

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
