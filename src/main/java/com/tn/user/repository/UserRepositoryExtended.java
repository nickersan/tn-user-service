package com.tn.user.repository;

import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;

public interface UserRepositoryExtended
{
  /**
   * Returns the user whose {@code identifierType} column already holds {@code identifierValue},
   * or creates a new user with that column set, atomically - safe under concurrent calls for the
   * same new value.
   */
  User findOrCreate(IdentifierType identifierType, String identifierValue);
}
