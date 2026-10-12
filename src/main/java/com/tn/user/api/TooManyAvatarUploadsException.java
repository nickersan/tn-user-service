package com.tn.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** The user has uploaded avatars more often than allowed, or the service is decoding as many as it will at once. */
@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyAvatarUploadsException extends RuntimeException
{
  public TooManyAvatarUploadsException(String message)
  {
    super(message);
  }
}
