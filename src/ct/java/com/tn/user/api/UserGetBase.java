package com.tn.user.api;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.reset;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.tn.service.data.api.DataApi;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

public abstract class UserGetBase extends Base
{
  private static final User USER_1 = new User(1L, "one.first@mail.com", "One", "First", "T1", created(1));
  private static final User USER_2 = new User(2L, "two.second@mail.com", "Two", "Second", "T2", created(2));
  private static final User USER_3 = new User(3L, "three.third@mail.com", "Three", "Third", "T3", created(3));

  @MockitoBean
  UserRepository userRepository;

  @Autowired
  DataApi dataApi;

  @BeforeEach
  void initializeUserApi()
  {
    RestAssuredMockMvc.standaloneSetup(dataApi);
  }

  @BeforeEach
  void initializeUserRepository()
  {
    lenient().when(userRepository.findAll(Sort.by(ASC, User.Fields.id))).thenReturn(List.of(USER_1, USER_2, USER_3));

  }

  @AfterEach
  void cleanUp()
  {
    reset(userRepository);
  }

  private static LocalDateTime created(int offset)
  {
    return LocalDateTime.of(2025, Month.JUNE, 20, 17, 30 + offset, offset);
  }
}
