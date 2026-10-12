package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Replaces the single {@code phone} column with {@code phone_country_code} and {@code phone_number},
 * unique as a pair. A stored value is read as an international number, with the {@code +} it may
 * lack. A value that can't be read, or two users who turn out to hold the same number, fail the
 * migration with the user ids to fix by hand.
 */
public class V1_0_01__split_phone_into_country_code_and_number extends BaseJavaMigration
{
  private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

  @Override
  public void migrate(Context context) throws Exception
  {
    Connection connection = context.getConnection();

    try (Statement statement = connection.createStatement())
    {
      statement.execute("ALTER TABLE users ADD COLUMN phone_country_code SMALLINT, ADD COLUMN phone_number VARCHAR(17)");
    }

    List<Long> unparsable = new ArrayList<>();
    Map<String, List<Long>> usersByNumber = new LinkedHashMap<>();
    Map<Long, PhoneNumber> numbers = new LinkedHashMap<>();

    try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery("SELECT user_id, phone FROM users WHERE phone IS NOT NULL"))
    {
      while (rows.next())
      {
        long userId = rows.getLong("user_id");
        PhoneNumber number = parse(rows.getString("phone"));

        if (number == null)
        {
          unparsable.add(userId);
          continue;
        }

        numbers.put(userId, number);
        usersByNumber.computeIfAbsent(number.getCountryCode() + ":" + PHONE_NUMBER_UTIL.getNationalSignificantNumber(number), key -> new ArrayList<>()).add(userId);
      }
    }

    List<List<Long>> duplicates = usersByNumber.values().stream().filter(userIds -> userIds.size() > 1).toList();

    if (!unparsable.isEmpty() || !duplicates.isEmpty())
    {
      throw new IllegalStateException("Cannot split phone numbers. Users with a phone number that can't be read: " + unparsable + ". Groups of users with the same phone number: " + duplicates);
    }

    try (PreparedStatement update = connection.prepareStatement("UPDATE users SET phone_country_code = ?, phone_number = ? WHERE user_id = ?"))
    {
      for (Map.Entry<Long, PhoneNumber> entry : numbers.entrySet())
      {
        update.setInt(1, entry.getValue().getCountryCode());
        update.setString(2, PHONE_NUMBER_UTIL.getNationalSignificantNumber(entry.getValue()));
        update.setLong(3, entry.getKey());
        update.executeUpdate();
      }
    }

    try (Statement statement = connection.createStatement())
    {
      statement.execute("ALTER TABLE users DROP COLUMN phone");
      statement.execute("ALTER TABLE users ADD CONSTRAINT users_phone_unique UNIQUE (phone_country_code, phone_number)");
      statement.execute("ALTER TABLE users ADD CONSTRAINT users_phone_both_or_neither CHECK ((phone_country_code IS NULL) = (phone_number IS NULL))");
    }
  }

  private PhoneNumber parse(String phone)
  {
    try
    {
      PhoneNumber number = PHONE_NUMBER_UTIL.parse(phone.startsWith("+") ? phone : "+" + phone, null);

      return PHONE_NUMBER_UTIL.isValidNumber(number) ? number : null;
    }
    catch (NumberParseException e)
    {
      return null;
    }
  }
}
