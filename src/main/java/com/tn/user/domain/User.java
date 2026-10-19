package com.tn.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.id.Tsid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Cacheable(false)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
@FieldNameConstants
public class User
{
  @Id
  @Tsid
  @Column(name = "user_id")
  @JsonProperty
  private Long id;

  @Email
  @JsonProperty
  private String email;

  @Valid
  @Embedded
  @JsonProperty
  private Phone phone;

  @Column(name = "full_name", length = 100)
  @JsonProperty
  private String fullName;

  @Column(name = "preferred_name", length = 100)
  @JsonProperty
  private String preferredName;

  @CreatedDate
  @JsonProperty
  private LocalDateTime created;

  /** The version of the user's avatar, which changes with each new one; absent when they have none. */
  @Column(name = "avatar_version", updatable = false)
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer avatarVersion;

  /** How many avatars the user has ever set. Never goes down, so that a version is never reused. */
  @Column(name = "avatar_revision", updatable = false)
  @JsonIgnore
  private int avatarRevision;

  /**
   * The id of the user's avatar in tn-media-service, which holds the picture; absent when they have none. A media item
   * never changes, so the id is also the version: a different picture is a different id.
   */
  @Column(name = "avatar_media_id", updatable = false)
  @JsonProperty
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Long avatarMediaId;

  public User(String email, Phone phone, String fullName, String preferredName)
  {
    this.email = email;
    this.phone = phone;
    this.fullName = fullName;
    this.preferredName = preferredName;
  }

  public User(Long id, String email, Phone phone, String fullName, String preferredName, LocalDateTime created)
  {
    this(id, email, phone, fullName, preferredName, created, null, 0, null);
  }

  public User(Long id, String email, Phone phone, String fullName, String preferredName, LocalDateTime created, Integer avatarVersion, int avatarRevision)
  {
    this(id, email, phone, fullName, preferredName, created, avatarVersion, avatarRevision, null);
  }

  /** The same user with new details: who they are, when they were created and their avatar stay. */
  public User with(String email, Phone phone, String fullName, String preferredName)
  {
    return new User(id, email, phone, fullName, preferredName, created, avatarVersion, avatarRevision, avatarMediaId);
  }
}
