package com.nttdata.bootcamp.mswallet.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DebitCardDTO {

    private String cardNumber;
    private Long clientId;
}
