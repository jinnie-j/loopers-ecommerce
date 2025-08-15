package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor
public class PointEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long balance;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    public PointEntity(long userId, long balance) {
        if(balance < 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.userId = userId;
        this.balance = balance;
    }

    public void charge(long amount){
        if(amount <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.balance += amount;
    }

    public void use(long amount){
        if(amount <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if(this.balance < amount){
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다.");
        }
        this.balance -= amount;
    }

}
