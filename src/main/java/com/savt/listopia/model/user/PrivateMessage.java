package com.savt.listopia.model.user;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "private_messages")
public class PrivateMessage {
    @Id
    @GeneratedValue
    Long id;

    Long fromUserId;

    Long toUserId;

    Long sentAtTimestampSeconds;

    Boolean isReported = false;

    @Column(columnDefinition = "TEXT", length = 1024)
    String message;
}
