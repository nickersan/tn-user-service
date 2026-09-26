package com.tn.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import static com.tn.user.domain.IdentifierType.EMAIL;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tn.user.controllers.UserActionsController;
import com.tn.user.controllers.UserController;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext
public abstract class AbstractContractTest extends AbstractPostgresIntegrationTest
{
  private static final User USER = new User(1L, "test@testing.com", null, "Test Tester", "Test", LocalDateTime.of(2026, 1, 1, 12, 0));
  private static final User NEW_USER = new User(2L, "new@testing.com", null, null, null, LocalDateTime.of(2026, 1, 1, 12, 0));

  @Autowired
  UserController userController;

  @Autowired
  UserActionsController userActionsController;

  @MockitoBean
  UserRepository userRepository;

  @BeforeEach
  public void setup()
  {
    when(userRepository.findById(1L)).thenReturn(Optional.of(USER));
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(USER)));
    when(userRepository.save(any(User.class))).thenReturn(NEW_USER);
    when(userRepository.findOrCreate(eq(EMAIL), eq("new@testing.com"))).thenReturn(NEW_USER);

    RestAssuredMockMvc.standaloneSetup(
      MockMvcBuilders.standaloneSetup(this.userController, this.userActionsController)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
    );
  }
}
