package com.tn.user.repository;

import jakarta.persistence.EntityManager;

import io.hypersistence.tsid.TSID;
import com.tn.query.jpa.AbstractQueryableRepository;
import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;

public class UserRepositoryImpl extends AbstractQueryableRepository<User> implements UserRepositoryCustom
{
  private static final String FIND_OR_CREATE = """
    INSERT INTO users (user_id, identifier_type, identifier_value, created)
    VALUES (:id, :identifierType, :identifierValue, CURRENT_TIMESTAMP)
    ON CONFLICT (identifier_type, identifier_value)
    DO UPDATE SET identifier_type = EXCLUDED.identifier_type
    RETURNING *
    """;

  public UserRepositoryImpl(EntityManager entityManager)
  {
    super(entityManager);
  }

  @Override
  public User findOrCreate(IdentifierType identifierType, String identifierValue)
  {
    return (User)entityManager().createNativeQuery(FIND_OR_CREATE, User.class)
      .setParameter("id", TSID.fast().toLong())
      .setParameter("identifierType", identifierType.name())
      .setParameter("identifierValue", identifierValue)
      .getSingleResult();
  }
}
