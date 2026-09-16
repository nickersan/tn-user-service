package com.tn.user.repository;

import com.tn.user.domain.IdentifierType;
import com.tn.user.domain.User;

public interface UserRepositoryCustom
{
  User findOrCreate(IdentifierType identifierType, String identifierValue);
}
