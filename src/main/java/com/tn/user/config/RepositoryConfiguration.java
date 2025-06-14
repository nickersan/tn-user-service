package com.tn.user.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tn.service.data.jpa.repository.DataRepositoryAdaptor;
import com.tn.service.data.parameter.IdentityParser;
import com.tn.service.data.parameter.LongIdentityParser;
import com.tn.service.data.repository.DataRepository;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;
import com.tn.user.repository.UserRepositoryImpl;

@Configuration
class RepositoryConfiguration
{
  private static final String FIELD_ID = "id";

  @Bean
  IdentityParser<Long> userKeyParser()
  {
    return new LongIdentityParser();
  }

  @Bean
  DataRepository<Long, User> userDataRepository(UserRepository userRepository)
  {
    return new DataRepositoryAdaptor<>(userRepository, FIELD_ID);
  }

  @Bean
  UserRepositoryImpl userRepositoryImpl(EntityManager entityManager)
  {
    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);

    return new UserRepositoryImpl(entityManager);
  }
}
