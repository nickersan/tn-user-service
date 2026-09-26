package com.tn.user.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

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

  @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "must be a valid phone number")
  @JsonProperty
  private String phone;

  @Column(name = "full_name", length = 100)
  @JsonProperty
  private String fullName;

  @Column(name = "preferred_name", length = 100)
  @JsonProperty
  private String preferredName;

  @CreatedDate
  @JsonProperty
  private LocalDateTime created;

  public User(String email, String phone, String fullName, String preferredName)
  {
    this.email = email;
    this.phone = phone;
    this.fullName = fullName;
    this.preferredName = preferredName;
  }
}
