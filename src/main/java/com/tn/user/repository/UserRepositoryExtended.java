package com.tn.user.repository;

import com.tn.user.domain.Phone;
import com.tn.user.domain.User;

public interface UserRepositoryExtended
{
  /**
   * Returns the user whose email is {@code email}, or creates a new user with that email,
   * atomically - safe under concurrent calls for the same new value.
   */
  User findOrCreateByEmail(String email);

  /**
   * Returns the user whose phone number is {@code phone}, or creates a new user with that phone
   * number, atomically - safe under concurrent calls for the same new value.
   */
  User findOrCreateByPhone(Phone phone);
}
