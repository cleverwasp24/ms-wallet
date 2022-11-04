package com.nttdata.bootcamp.mswallet.service;

import com.nttdata.bootcamp.mswallet.dto.*;
import com.nttdata.bootcamp.mswallet.model.Transaction;
import com.nttdata.bootcamp.mswallet.dto.TransactionDTO;
import com.nttdata.bootcamp.mswallet.dto.TransferDTO;
import com.nttdata.bootcamp.mswallet.model.Transaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface TransactionService {

    Flux<Transaction> findAll();

    Mono<Transaction> create(Transaction transaction);

    Mono<Transaction> findById(Long id);

    Mono<Transaction> update(Long id, Transaction transaction);

    Mono<Void> delete(Long id);

    Mono<String> withdraw(TransactionDTO transactionDTO);

    Mono<String> deposit(TransactionDTO transactionDTO);

    Mono<String> transfer(TransferDTO transferDTO);

    Flux<Transaction> findAllByWalletId(Long walletId);

    Flux<Transaction> findAllByWalletIdDesc(Long walletId);

    Flux<Transaction> findTransactionsWalletMonth(Long walletId, LocalDateTime date);

    Flux<Transaction> findTransactionsWalletPeriod(Long walletId, LocalDateTime start, LocalDateTime end);

    Mono<String> checkFields(Transaction transaction);

    Mono<CompleteReportDTO> generateCompleteReport(Long id, PeriodDTO periodDTO);

    Mono<Transaction> findLastTransactionBefore(Long id, LocalDateTime date);

    Mono<WalletReportDTO> generateWalletReportCurrentMonth(Long id);

    Mono<WalletReportDTO> generateWalletReport(Long id, PeriodDTO periodDTO);
}
