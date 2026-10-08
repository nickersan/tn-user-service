package com.tn.user.config;

import jakarta.persistence.EntityManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tn.service.data.parameter.QueryBuilder;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepositoryImpl;

@Configuration
class ServiceConfiguration
{
  @Bean
  QueryBuilder queryBuilder()
  {
    return new QueryBuilder(User.class);
  }

  @Bean
  UserRepositoryImpl userRepositoryImpl(EntityManager entityManager)
  {
    return new UserRepositoryImpl(entityManager);
  }
}
