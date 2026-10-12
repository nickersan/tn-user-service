package com.tn.user.domain;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneValidator implements ConstraintValidator<ValidPhone, Phone>
{
  private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

  @Override
  public boolean isValid(Phone phone, ConstraintValidatorContext context)
  {
    if (phone == null) return true;
    if (phone.countryCode() == null || phone.number() == null) return false;
    if (!phone.number().matches("[0-9]{1,17}")) return false;

    return isValidNationalSignificantNumber(phone.countryCode(), phone.number());
  }

  /**
   * Valid for its country, and already in national significant form: parsing strips a trunk
   * prefix, so a number that comes back different was not given in that form.
   */
  private boolean isValidNationalSignificantNumber(int countryCode, String number)
  {
    try
    {
      PhoneNumber parsed = PHONE_NUMBER_UTIL.parse("+" + countryCode + number, null);

      return parsed.getCountryCode() == countryCode
        && PHONE_NUMBER_UTIL.isValidNumber(parsed)
        && PHONE_NUMBER_UTIL.getNationalSignificantNumber(parsed).equals(number);
    }
    catch (NumberParseException e)
    {
      return false;
    }
  }
}
