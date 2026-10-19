package com.tn.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.tn.user.api.UserNotFoundException;
import com.tn.user.controller.AvatarMediaController;
import com.tn.user.controller.UserActionsController;
import com.tn.user.controller.UserController;
import com.tn.user.domain.Phone;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext
public abstract class AbstractContractTest extends AbstractPostgresIntegrationTest
{
  private static final User USER = new User(1L, "test@testing.com", null, "Test Tester", "Test", LocalDateTime.of(2026, 1, 1, 12, 0));
  private static final User NEW_USER = new User(2L, "new@testing.com", null, null, null, LocalDateTime.of(2026, 1, 1, 12, 0));
  private static final Phone PHONE = new Phone(44, "7911123456");
  private static final User NEW_PHONE_USER = new User(3L, null, PHONE, null, null, LocalDateTime.of(2026, 1, 1, 12, 0));
  private static final User UPDATED_USER = new User(1L, "test@testing.com", PHONE, "Test Tester", "Test", LocalDateTime.of(2026, 1, 1, 12, 0));
  private static final String PHONE_OF_ANOTHER_USER = "7400123456";
  // the user that has the phone number above, and has never gone further than signing up with it
  private static final User PHONE_OF_ANOTHER_USER_HOLDER = new User(4L, null, new Phone(44, PHONE_OF_ANOTHER_USER), null, null, LocalDateTime.of(2026, 1, 1, 12, 0));

  @Autowired
  UserController userController;

  // the user an avatar item has been set for, and the one it has been cleared for
  private static final User USER_WITH_AVATAR_MEDIA = new User(7L, "media@testing.com", null, "Media Tester", "Media", LocalDateTime.of(2026, 1, 1, 12, 0), 123L);
  private static final User USER_WITHOUT_AVATAR_MEDIA = new User(8L, "nomedia@testing.com", null, null, null, LocalDateTime.of(2026, 1, 1, 12, 0));

  @Autowired
  UserActionsController userActionsController;

  @Autowired
  AvatarMediaController avatarMediaController;

  @MockitoBean
  UserRepository userRepository;

  @BeforeEach
  public void setup()
  {

    when(userRepository.existsById(7L)).thenReturn(true);
    when(userRepository.existsById(8L)).thenReturn(true);
    when(userRepository.existsById(999L)).thenReturn(false);
    when(userRepository.findById(7L)).thenReturn(Optional.of(USER_WITH_AVATAR_MEDIA));
    when(userRepository.findById(8L)).thenReturn(Optional.of(USER_WITHOUT_AVATAR_MEDIA));
    when(userRepository.findById(1L)).thenReturn(Optional.of(USER));
    when(userRepository.findById(4L)).thenReturn(Optional.of(PHONE_OF_ANOTHER_USER_HOLDER));
    when(userRepository.findById(999L)).thenReturn(Optional.empty());
    when(userRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(USER)));
    when(userRepository.save(any(User.class))).thenReturn(NEW_USER);
    when(userRepository.save(argThat((User user) -> user != null && user.phone() != null))).thenReturn(NEW_PHONE_USER);
    when(userRepository.save(argThat((User user) -> user != null && Long.valueOf(1L).equals(user.id()) && PHONE.equals(user.phone())))).thenReturn(UPDATED_USER);
    when(userRepository.save(argThat((User user) -> user != null && user.phone() != null && PHONE_OF_ANOTHER_USER.equals(user.phone().number())))).thenThrow(new DataIntegrityViolationException("phone"));
    when(userRepository.findOrCreateByEmail(eq("new@testing.com"))).thenReturn(NEW_USER);
    when(userRepository.findOrCreateByPhone(eq(PHONE))).thenReturn(NEW_PHONE_USER);
    when(userRepository.findOrCreateByPhone(eq(new Phone(44, PHONE_OF_ANOTHER_USER)))).thenReturn(PHONE_OF_ANOTHER_USER_HOLDER);

    RestAssuredMockMvc.standaloneSetup(
      MockMvcBuilders.standaloneSetup(this.userController, this.userActionsController, this.avatarMediaController)
        .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
    );
  }
}
