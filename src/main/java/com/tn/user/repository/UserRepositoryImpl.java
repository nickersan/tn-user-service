package com.tn.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import io.hypersistence.tsid.TSID;
import org.springframework.transaction.annotation.Transactional;

import com.tn.query.jpa.AbstractQueryableRepository;
import com.tn.user.domain.Phone;
import com.tn.user.domain.User;

public class UserRepositoryImpl extends AbstractQueryableRepository<User> implements UserRepositoryExtended
{
  private static final String FIND_OR_CREATE_EMAIL = """
    INSERT INTO users (user_id, email, created)
    VALUES (:id, :email, CURRENT_TIMESTAMP)
    ON CONFLICT (email)
    DO UPDATE SET email = EXCLUDED.email
    RETURNING *
    """;

  private static final String FIND_OR_CREATE_PHONE = """
    INSERT INTO users (user_id, phone_country_code, phone_number, created)
    VALUES (:id, :countryCode, :number, CURRENT_TIMESTAMP)
    ON CONFLICT (phone_country_code, phone_number)
    DO UPDATE SET phone_country_code = EXCLUDED.phone_country_code
    RETURNING *
    """;

  public UserRepositoryImpl(EntityManager entityManager)
  {
    super(entityManager);
  }

  @Override
  @Transactional
  public User findOrCreateByEmail(String email)
  {
    return findOrCreate(entityManager().createNativeQuery(FIND_OR_CREATE_EMAIL, User.class).setParameter("email", email));
  }

  @Override
  @Transactional
  public User findOrCreateByPhone(Phone phone)
  {
    return findOrCreate(
      entityManager().createNativeQuery(FIND_OR_CREATE_PHONE, User.class)
        .setParameter("countryCode", phone.countryCode())
        .setParameter("number", phone.number())
    );
  }

  private User findOrCreate(Query query)
  {
    User user = (User)query.setParameter("id", TSID.fast().toLong()).getSingleResult();

    // the native query bypasses the persistence context - clear it so a later read in the same
    // transaction doesn't see a stale cached entity
    entityManager().clear();

    return user;
  }
}
