package com.nttdata.bootcamp.mswallet.dto;

import com.nttdata.bootcamp.mswallet.model.Transaction;
import com.nttdata.bootcamp.mswallet.model.Wallet;
import lombok.Data;

import java.util.List;

@Data
public class CompleteReportDTO {

    private Wallet wallet;
    private List<Transaction> transactions;

}
