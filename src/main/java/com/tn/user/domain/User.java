package com.tn.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString
@FieldNameConstants
public class User
{
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userId")
  @SequenceGenerator(name = "userId", sequenceName = "user_id_seq", allocationSize = 1)
  @Column(name = "user_id")
  @JsonProperty
  private Long id;

  @Column(nullable = false, unique = true)
  @JsonProperty
  @Email
  @NotNull
  private String email;

  @Column(name = "first_name", nullable = false, length = 100)
  @JsonProperty
  @NotNull
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  @JsonProperty
  @NotNull
  private String lastName;

  @Column(name = "token_subject", length = 100)
  @JsonProperty
  private String tokenSubject;

  @CreatedDate
  @JsonProperty
  private LocalDateTime created;

  public User(String email, String firstName, String lastName, String tokenSubject)
  {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.tokenSubject = tokenSubject;
  }
}
