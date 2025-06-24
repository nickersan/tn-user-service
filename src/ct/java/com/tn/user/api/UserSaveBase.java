package com.tn.user.api;

import static org.mockito.Mockito.reset;

import static com.tn.service.data.jpa.repository.QueryablePagingAndSortingCrudRepositoryMocks.initializeSaveMethods;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.concurrent.atomic.AtomicInteger;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tn.service.data.api.DataApi;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

public abstract class UserSaveBase extends Base
{
  @MockitoBean
  UserRepository userRepository;

  @Autowired
  DataApi dataApi;

  private final AtomicInteger counter = new AtomicInteger(0);

  @BeforeEach
  void initializeUserApi()
  {
    RestAssuredMockMvc.standaloneSetup(dataApi);
  }

  @BeforeEach
  void initializeUserRepository()
  {
    initializeSaveMethods(userRepository, this::save);
  }

  @BeforeEach
  void resetCounter()
  {
    counter.set(0);
  }

  @AfterEach
  void cleanUp()
  {
    reset(userRepository);
  }

  private User save(User user)
  {
    int count = counter.incrementAndGet();
    return new User((long)count, user.email(), user.fullName(), user.preferredName(), user.tokenSubject(), created(count));
  }

  private static LocalDateTime created(int offset)
  {
    return LocalDateTime.of(2025, Month.JUNE, 20, 17, 30 + offset, offset);
  }
}
