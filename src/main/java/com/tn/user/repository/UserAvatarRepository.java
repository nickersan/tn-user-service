package com.tn.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tn.user.domain.UserAvatar;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long>
{
}
