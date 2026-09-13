package com.project.nyamtori.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.ConnectionBuilder;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "likes")
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    private LocalDateTime likedAt;

    @Builder
    public Like(User user, Recipe recipe) {
        this.user = user;
        this.recipe = recipe;
        this.likedAt = LocalDateTime.now();
    }

}
