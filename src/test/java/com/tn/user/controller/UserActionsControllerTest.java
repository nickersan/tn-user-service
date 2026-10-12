package com.tn.user.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import com.tn.user.api.MissingIdentifierException;
import com.tn.user.api.UserActionsApi.FindOrCreateRequest;
import com.tn.user.domain.Phone;
import com.tn.user.repository.UserRepository;

class UserActionsControllerTest
{
  private final UserRepository userRepository = mock(UserRepository.class);
  private final UserActionsController controller = new UserActionsController(userRepository);

  @Test
  void shouldRejectFindOrCreateWithBothAnEmailAndAPhone()
  {
    assertThrows(MissingIdentifierException.class, () -> controller.findOrCreate(new FindOrCreateRequest("test@testing.com", new Phone(44, "7911123456"))));

    verifyNoInteractions(userRepository);
  }

  @Test
  void shouldRejectFindOrCreateWithNeither()
  {
    assertThrows(MissingIdentifierException.class, () -> controller.findOrCreate(new FindOrCreateRequest(null, null)));

    verifyNoInteractions(userRepository);
  }
}
