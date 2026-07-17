package com.programmers.kdt.order.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Point {

    @Id
    private Long userId;

    private Long point;
}
