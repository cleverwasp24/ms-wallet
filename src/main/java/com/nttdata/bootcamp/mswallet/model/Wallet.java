package com.nttdata.bootcamp.mswallet.model;

import com.mongodb.lang.NonNull;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "wallet")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Log4j2
public class Wallet {

    @Transient
    public static final String SEQUENCE_NAME = "wallet_sequence";

    @Id
    private Long id;
    @NonNull
    private Long clientId;
    @NonNull
    @Indexed(unique = true)
    private String walletNumber;
    @NonNull
    private Double balance;
    @NonNull
    private Double initialBalance;
    @NonNull
    private Long debitCardId;
    @NonNull
    private String imeiNumber;
    @NonNull
    private String phoneNumber;
    @NonNull
    private LocalDateTime creationDate;

}
