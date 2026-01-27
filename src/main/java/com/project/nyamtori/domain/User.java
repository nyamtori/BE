package com.project.nyamtori.domain;

import com.project.nyamtori.common.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String nickname;

    @Column(unique = true)
    private String kakaoId;
}
