package com.project.nyamtori.domain;

import com.project.nyamtori.common.BaseTimeEntity;
import com.project.nyamtori.domain.User.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Refrigerator extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userMaterialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int quantity;

    private LocalDate expireDate;

    @Column(columnDefinition = "TEXT")
    private String etc;
}
