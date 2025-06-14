package com.tn.user.repository;

import jakarta.persistence.EntityManager;

import com.tn.query.jpa.AbstractQueryableRepository;
import com.tn.user.domain.User;

public class UserRepositoryImpl extends AbstractQueryableRepository<User>
{
  public UserRepositoryImpl(EntityManager entityManager)
  {
    super(entityManager);
  }
}
