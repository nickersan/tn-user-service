package com.tn.user.api;

import java.time.LocalDateTime;

import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;

public record UserResponse(
  Long id,
  IdentifierType identifierType,
  String identifierValue,
  String fullName,
  String preferredName,
  String tokenSubject,
  LocalDateTime created
)
{
  public static UserResponse from(User user)
  {
    return new UserResponse(user.id(), user.identifierType(), user.identifierValue(), user.fullName(), user.preferredName(), user.tokenSubject(), user.created());
  }
}
