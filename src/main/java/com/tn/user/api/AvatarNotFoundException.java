package com.tn.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AvatarNotFoundException extends RuntimeException
{
  public AvatarNotFoundException(long userId)
  {
    super("No avatar found for user: " + userId);
  }
}
