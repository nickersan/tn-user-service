package com.tn.user.repository;

import org.springframework.data.repository.CrudRepository;

import com.tn.query.jpa.QueryableRepository;
import com.tn.user.domain.User;

public interface UserRepository extends CrudRepository<User, Long>, QueryableRepository<User> {}