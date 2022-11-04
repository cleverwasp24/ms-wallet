package com.nttdata.bootcamp.mswallet.dto;

import com.nttdata.bootcamp.mswallet.model.Wallet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WalletReportDTO {

    private Wallet wallet;
    private List<DailyBalanceDTO> dailyBalances = new ArrayList<>();

}
