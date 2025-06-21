package com.tn.user.repository;

import com.tn.service.data.jpa.repository.QueryablePagingAndSortingCrudRepository;
import com.tn.user.domain.User;

public interface UserRepository extends QueryablePagingAndSortingCrudRepository<User, Long> {}