package com.lht.lhtrpc.demo.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Leo
 * @date 2024/03/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order {

    private int id;
    private double amount;


}
