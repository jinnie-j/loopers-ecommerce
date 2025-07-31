package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor
public class PointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int balance;

    private Long userId;

    public PointEntity(int balance, Long userId) {
        if(balance < 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.balance = balance;
        this.userId = userId;
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
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.balance -= amount;
    }

}
