package com.nttdata.bootcamp.mswallet.dto;

import lombok.Data;

@Data
public class WalletDTO {

    private Long clientId;
    private String walletNumber;
    private Double balance;
    private Long debitCardId;
    private String imeiNumber;
    private String phoneNumber;

}
