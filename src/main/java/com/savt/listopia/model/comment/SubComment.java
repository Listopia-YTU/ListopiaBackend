package com.savt.listopia.model.comment;

import com.savt.listopia.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;

@EqualsAndHashCode(callSuper = false)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubComment extends Comment {
    @Column(columnDefinition = "TEXT", length = 1024)
    private String content;

    private LocalDateTime creationDate;

    private String profilePicture;

    private String profileName;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;
}
