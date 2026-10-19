package com.tn.user.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import io.hypersistence.tsid.TSID;
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
