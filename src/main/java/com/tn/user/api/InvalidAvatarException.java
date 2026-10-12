package com.tn.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** An upload that isn't an acceptable avatar: too large, not a JPEG or PNG, or claiming too many pixels. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAvatarException extends RuntimeException
{
  public InvalidAvatarException(String message)
  {
    super(message);
  }

  public InvalidAvatarException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
