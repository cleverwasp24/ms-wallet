package com.nttdata.bootcamp.mswallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyBalanceDTO {
    private LocalDate date;
    private Double balance;
}
