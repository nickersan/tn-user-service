package com.tn.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;

import com.tn.query.QueryParser;
import com.tn.query.jpa.AbstractQueryableRepository;
import com.tn.user.domain.User;

public class UserRepositoryImpl extends AbstractQueryableRepository<User>
{
  public UserRepositoryImpl(EntityManager entityManager, CriteriaQuery<User> criteriaQuery, QueryParser<Predicate> queryParser)
  {
    super(
      entityManager,
      criteriaQuery,
      queryParser
    );
  }
}
