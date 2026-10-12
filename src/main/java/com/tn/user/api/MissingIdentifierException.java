package com.tn.user.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingIdentifierException extends RuntimeException
{
  public MissingIdentifierException()
  {
    this("A user must have an email or a phone number");
  }

  public MissingIdentifierException(String message)
  {
    super(message);
  }
}
