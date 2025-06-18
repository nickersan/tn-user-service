package com.tn.user.config;

import jakarta.persistence.EntityManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tn.service.data.io.DefaultJsonCodec;
import com.tn.service.data.io.JsonCodec;
import com.tn.service.data.jpa.repository.DataRepositoryAdaptor;
import com.tn.service.data.parameter.IdentityParser;
import com.tn.service.data.parameter.LongIdentityParser;
import com.tn.service.data.parameter.QueryBuilder;
import com.tn.service.data.repository.DataRepository;
import com.tn.user.domain.User;
import com.tn.user.repository.UserRepository;
import com.tn.user.repository.UserRepositoryImpl;

@Configuration
class ServiceConfiguration
{
  private static final String FIELD_ID = "id";

  @Bean
  JsonCodec<User> jsonCodec(ObjectMapper objectMapper)
  {
    return new DefaultJsonCodec<>(objectMapper, User.class);
  }

  @Bean
  DataRepository<Long, User> userDataRepository(UserRepository userRepository)
  {
    return new DataRepositoryAdaptor<>(userRepository, FIELD_ID);
  }

  @Bean
  IdentityParser<String, Long> userIdentityParser()
  {
    return new LongIdentityParser();
  }

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
