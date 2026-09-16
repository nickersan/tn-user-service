package com.tn.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

  @Enumerated(EnumType.STRING)
  @Column(name = "identifier_type", nullable = false)
  @JsonProperty
  private IdentifierType identifierType;

  @Column(name = "identifier_value", nullable = false)
  @JsonProperty
  private String identifierValue;

  @Column(name = "full_name", length = 100)
  @JsonProperty
  private String fullName;

  @Column(name = "preferred_name", length = 100)
  @JsonProperty
  private String preferredName;

  @Column(name = "token_subject", length = 100)
  @JsonProperty
  private String tokenSubject;

  @CreatedDate
  @JsonProperty
  private LocalDateTime created;

  public User(IdentifierType identifierType, String identifierValue, String fullName, String preferredName, String tokenSubject)
  {
    this.identifierType = identifierType;
    this.identifierValue = identifierValue;
    this.fullName = fullName;
    this.preferredName = preferredName;
    this.tokenSubject = tokenSubject;
  }
}
