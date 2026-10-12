package com.tn.user.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhoneValidatorTest
{
  private final PhoneValidator validator = new PhoneValidator();

  @Test
  void shouldAcceptAValidUkMobile()
  {
    assertTrue(validator.isValid(new Phone(44, "7911123456"), null));
  }

  @Test
  void shouldAcceptAnItalianNumberKeepingItsLeadingZero()
  {
    assertTrue(validator.isValid(new Phone(39, "0612345678"), null));
  }

  @Test
  void shouldRejectAUkNumberGivenWithItsLeadingZero()
  {
    assertFalse(validator.isValid(new Phone(44, "07911123456"), null));
  }

  @Test
  void shouldRejectANumberTooShortForItsCountry()
  {
    assertFalse(validator.isValid(new Phone(44, "7911"), null));
  }

  @Test
  void shouldRejectOnlyOnePart()
  {
    assertFalse(validator.isValid(new Phone(44, null), null));
    assertFalse(validator.isValid(new Phone(null, "7911123456"), null));
  }

  @Test
  void shouldRejectAnUnknownCountryCode()
  {
    assertFalse(validator.isValid(new Phone(999, "7911123456"), null));
  }

  @Test
  void shouldRejectNonDigits()
  {
    assertFalse(validator.isValid(new Phone(44, "7700 900123"), null));
  }

  @Test
  void shouldAcceptNoPhoneAtAll()
  {
    assertTrue(validator.isValid(null, null));
  }
}
