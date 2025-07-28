package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point")
@Getter
@NoArgsConstructor
public class PointEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int balance;

    private String userId;

    public PointEntity(int balance, String userId) {
        if(balance < 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.balance = balance;
        this.userId = userId;
    }

    public void charge(int amount){
        if(amount <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        int newBalance = this.balance + amount;
        this.balance = newBalance;
    }

    public void use(int amount){
        if(amount <= 0){
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        int newBalance = this.balance - amount;
        this.balance = newBalance;
    }

}
