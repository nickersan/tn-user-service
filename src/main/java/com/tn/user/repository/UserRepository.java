package com.tn.user.repository;

import java.util.Optional;

import com.tn.service.data.jpa.repository.QueryablePagingAndSortingCrudRepository;
import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;

public interface UserRepository extends QueryablePagingAndSortingCrudRepository<User, Long>, UserRepositoryCustom
{
  Optional<User> findByIdentifierTypeAndIdentifierValue(IdentifierType identifierType, String identifierValue);
}