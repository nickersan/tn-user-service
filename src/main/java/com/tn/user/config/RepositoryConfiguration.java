package com.tn.user.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tn.query.DefaultQueryParser;
import com.tn.query.ValueMappers;
import com.tn.query.jpa.JpaPredicateFactory;
import com.tn.query.jpa.NameMappings;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepositoryImpl;

@Configuration
class RepositoryConfiguration
{
  @Bean
  UserRepositoryImpl userRepositoryImpl(EntityManager entityManager)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);

    return new UserRepositoryImpl(
      entityManager,
      criteriaQuery,
      new DefaultQueryParser<>(
        new JpaPredicateFactory(
          entityManager.getCriteriaBuilder(),
          NameMappings.forFields(User.class, criteriaQuery)
        ),
        ValueMappers.forFields(User.class)
      )
    );
  }
}
