package com.nttdata.bootcamp.mswallet.dto;

import lombok.Data;

@Data
public class TransactionDTO {

    private Long walletId;
    private Double amount;

}
