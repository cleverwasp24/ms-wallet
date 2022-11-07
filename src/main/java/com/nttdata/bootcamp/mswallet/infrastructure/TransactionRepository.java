package com.nttdata.bootcamp.mswallet.infrastructure;

import com.nttdata.bootcamp.mswallet.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, Long> {

    Flux<Transaction> findAllByWalletId(Long walletId);

    Flux<Transaction> findAllByWalletIdOrderByTransactionDateDesc(Long walletId);

    Flux<Transaction> findAllByWalletIdAndTransactionDateBetween(Long walletId, LocalDateTime start, LocalDateTime end);

    Mono<Transaction> findByWalletIdAndTransactionDateBeforeOrderByTransactionDateDesc(Long walletId, LocalDateTime date);

}
