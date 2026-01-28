package com.project.nyamtori.domain;

import com.project.nyamtori.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Ingredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ingredientId;

    private String ingredientName;

    private LocalDate ingredientDate;

    @Column(columnDefinition = "TEXT")
    private String ingredientEtc;

    private String imgUrl;

    private int amount;

    private String category;

    public void applyCreate(
            String ingredientName,
            LocalDate ingredientDate,
            String ingredientEtc,
            String imgUrl,
            int amount,
            String category
    ) {
        this.ingredientName = ingredientName;
        this.ingredientDate = ingredientDate;
        this.ingredientEtc = ingredientEtc;
        this.imgUrl = imgUrl;
        this.amount = amount;
        this.category = category;
    }


}
