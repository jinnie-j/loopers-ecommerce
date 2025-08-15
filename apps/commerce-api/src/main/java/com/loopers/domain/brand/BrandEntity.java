package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor
public class BrandEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    public BrandEntity(String name, String description) {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.name = name;
        this.description = description;
    }

    public static BrandEntity of(String name, String description) {
        return new BrandEntity(name, description);
    }

}
