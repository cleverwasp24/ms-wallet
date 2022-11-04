package com.nttdata.bootcamp.mswallet.model.enums;

import java.util.HashMap;
import java.util.Map;

public enum ClientTypeEnum {

    PERSONAL(0),
    BUSINESS(1);

    private int value;
    private static Map map = new HashMap();

    private ClientTypeEnum(int value) {
        this.value = value;
    }

    static {
        for (ClientTypeEnum clientType : ClientTypeEnum.values()) {
            map.put(clientType.value, clientType);
        }
    }

    public static ClientTypeEnum valueOf(int clientType) {
        return (ClientTypeEnum) map.get(clientType);
    }

    public int getValue() {
        return value;
    }

}
