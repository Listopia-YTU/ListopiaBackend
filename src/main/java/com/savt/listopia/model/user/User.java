package com.savt.listopia.model.user;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    Long id;

    UUID uuid = UUID.randomUUID();

    String username;
    String firstName;
    String lastName;

    String email;
    String hashedPassword;
}
