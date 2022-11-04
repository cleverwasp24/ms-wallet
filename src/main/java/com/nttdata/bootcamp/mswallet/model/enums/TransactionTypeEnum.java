package com.nttdata.bootcamp.mswallet.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum TransactionTypeEnum {

    DEPOSIT(0),

    WITHDRAW(1),

    TRANSFER(2);

    private int value;
    private static Map map = new HashMap();

    private TransactionTypeEnum(int value) {
        this.value = value;
    }

    static {
        for (TransactionTypeEnum transactionType : TransactionTypeEnum.values()) {
            map.put(transactionType.value, transactionType);
        }
    }

    public static TransactionTypeEnum valueOf(int transactionType) {
        return (TransactionTypeEnum) map.get(transactionType);
    }

    public int getValue() {
        return value;
    }


}
