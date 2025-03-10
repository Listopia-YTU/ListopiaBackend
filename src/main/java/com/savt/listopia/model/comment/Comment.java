package com.savt.listopia.model.comment;

import com.savt.listopia.model.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public abstract class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    private String content;

    private LocalDateTime creationDate;

    private String profilePicture;

    private String profileName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany
    @JoinTable(name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "comment_like_id"))
    private HashSet<CommentLike> commentLikes = new HashSet<>();
}
