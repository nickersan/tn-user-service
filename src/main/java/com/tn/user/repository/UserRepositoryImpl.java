package com.tn.user.repository;

import jakarta.persistence.EntityManager;

import io.hypersistence.tsid.TSID;
import org.springframework.transaction.annotation.Transactional;

import com.tn.query.jpa.AbstractQueryableRepository;
import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;

public class UserRepositoryImpl extends AbstractQueryableRepository<User> implements UserRepositoryExtended
{
  private static final String FIND_OR_CREATE_EMAIL = """
    INSERT INTO users (user_id, email, created)
    VALUES (:id, :value, CURRENT_TIMESTAMP)
    ON CONFLICT (email)
    DO UPDATE SET email = EXCLUDED.email
    RETURNING *
    """;

  private static final String FIND_OR_CREATE_PHONE = """
    INSERT INTO users (user_id, phone, created)
    VALUES (:id, :value, CURRENT_TIMESTAMP)
    ON CONFLICT (phone)
    DO UPDATE SET phone = EXCLUDED.phone
    RETURNING *
    """;

  public UserRepositoryImpl(EntityManager entityManager)
  {
    super(entityManager);
  }

  @Override
  @Transactional
  public User findOrCreate(IdentifierType identifierType, String identifierValue)
  {
    User user = (User)entityManager().createNativeQuery(findOrCreateSql(identifierType), User.class)
      .setParameter("id", TSID.fast().toLong())
      .setParameter("value", identifierValue)
      .getSingleResult();

    // the native query bypasses the persistence context - clear it so a later read in the same
    // transaction doesn't see a stale cached entity
    entityManager().clear();

    return user;
  }

  private String findOrCreateSql(IdentifierType identifierType)
  {
    return switch (identifierType)
    {
      case EMAIL -> FIND_OR_CREATE_EMAIL;
      case PHONE -> FIND_OR_CREATE_PHONE;
    };
  }
}
