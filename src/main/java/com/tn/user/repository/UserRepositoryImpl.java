package com.tn.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.Duration;
import java.time.LocalDateTime;

import io.hypersistence.tsid.TSID;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tn.query.jpa.AbstractQueryableRepository;
import com.tn.user.domain.Phone;
import com.tn.user.domain.User;

public class UserRepositoryImpl extends AbstractQueryableRepository<User> implements UserRepositoryExtended
{
  private static final String FIND_OR_CREATE_EMAIL = """
    INSERT INTO users (user_id, email, created)
    VALUES (:id, :email, CURRENT_TIMESTAMP)
    ON CONFLICT (email)
    DO UPDATE SET email = EXCLUDED.email
    RETURNING *
    """;

  private static final String FIND_OR_CREATE_PHONE = """
    INSERT INTO users (user_id, phone_country_code, phone_number, created)
    VALUES (:id, :countryCode, :number, CURRENT_TIMESTAMP)
    ON CONFLICT (phone_country_code, phone_number)
    DO UPDATE SET phone_country_code = EXCLUDED.phone_country_code
    RETURNING *
    """;

  // starts a new window when the last has ended; otherwise counts in it - and updates nothing once the limit is reached
  private static final String RESERVE_AVATAR_UPLOAD = """
    UPDATE users
    SET avatar_window_count = CASE WHEN avatar_window_start IS NULL OR avatar_window_start <= :cutoff THEN 1 ELSE avatar_window_count + 1 END,
        avatar_window_start = CASE WHEN avatar_window_start IS NULL OR avatar_window_start <= :cutoff THEN :now ELSE avatar_window_start END
    WHERE user_id = :id
      AND (avatar_window_start IS NULL OR avatar_window_start <= :cutoff OR avatar_window_count < :limit)
    """;

  // both sides of a SET read the row as it was, so version and revision become the same new number
  private static final String NEXT_AVATAR_VERSION = """
    UPDATE users
    SET avatar_revision = avatar_revision + 1, avatar_version = avatar_revision + 1
    WHERE user_id = :id
    RETURNING avatar_version
    """;

  private static final String CLEAR_AVATAR_VERSION = "UPDATE users SET avatar_version = NULL WHERE user_id = :id";

  private static final String SET_AVATAR_MEDIA = "UPDATE users SET avatar_media_id = :mediaId WHERE user_id = :id";
  private static final String CLEAR_AVATAR_MEDIA = "UPDATE users SET avatar_media_id = NULL WHERE user_id = :id";

  public UserRepositoryImpl(EntityManager entityManager)
  {
    super(entityManager);
  }

  @Override
  @Transactional
  public User findOrCreateByEmail(String email)
  {
    return findOrCreate(entityManager().createNativeQuery(FIND_OR_CREATE_EMAIL, User.class).setParameter("email", email));
  }

  @Override
  @Transactional
  public User findOrCreateByPhone(Phone phone)
  {
    return findOrCreate(
      entityManager().createNativeQuery(FIND_OR_CREATE_PHONE, User.class)
        .setParameter("countryCode", phone.countryCode())
        .setParameter("number", phone.number())
    );
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean reserveAvatarUpload(long id, int limit, Duration window)
  {
    LocalDateTime now = LocalDateTime.now();

    return entityManager().createNativeQuery(RESERVE_AVATAR_UPLOAD)
      .setParameter("id", id)
      .setParameter("now", now)
      .setParameter("cutoff", now.minus(window))
      .setParameter("limit", limit)
      .executeUpdate() == 1;
  }

  @Override
  @Transactional
  public int nextAvatarVersion(long id)
  {
    int version = ((Number)entityManager().createNativeQuery(NEXT_AVATAR_VERSION).setParameter("id", id).getSingleResult()).intValue();

    // the native query bypasses the persistence context - see findOrCreate
    entityManager().clear();

    return version;
  }

  @Override
  @Transactional
  public void clearAvatarVersion(long id)
  {
    entityManager().createNativeQuery(CLEAR_AVATAR_VERSION).setParameter("id", id).executeUpdate();
    entityManager().clear();
  }

  @Override
  @Transactional
  public void setAvatarMedia(long id, long mediaId)
  {
    entityManager().createNativeQuery(SET_AVATAR_MEDIA).setParameter("id", id).setParameter("mediaId", mediaId).executeUpdate();
    entityManager().clear();
  }

  @Override
  @Transactional
  public void clearAvatarMedia(long id)
  {
    entityManager().createNativeQuery(CLEAR_AVATAR_MEDIA).setParameter("id", id).executeUpdate();
    entityManager().clear();
  }

  private User findOrCreate(Query query)
  {
    User user = (User)query.setParameter("id", TSID.fast().toLong()).getSingleResult();

    // the native query bypasses the persistence context - clear it so a later read in the same
    // transaction doesn't see a stale cached entity
    entityManager().clear();

    return user;
  }
}
