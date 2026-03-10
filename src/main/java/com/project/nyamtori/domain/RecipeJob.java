package com.project.nyamtori.domain;

import com.project.nyamtori.common.BaseTimeEntity;
import com.project.nyamtori.enums.JobStatus;
import com.project.nyamtori.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RecipeJob extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Column(columnDefinition = "Text")
    private String ingredientsJsons;

    @Column(columnDefinition = "Text")
    private String result;

    private Integer cookTime;

}
