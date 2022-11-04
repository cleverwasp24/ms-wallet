package com.nttdata.bootcamp.mswallet.infrastructure;

import com.nttdata.bootcamp.mswallet.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends ReactiveMongoRepository<Transaction, Long> {

    Flux<Transaction> findAllByWalletId(Long accountId);

    Flux<Transaction> findAllByWalletIdOrderByTransactionDateDesc(Long accountId);

    Flux<Transaction> findAllByWalletIdAndTransactionDateBetween(Long accountId, LocalDateTime start, LocalDateTime end);

    Mono<Transaction> findByWalletIdAndTransactionDateBeforeOrderByTransactionDateDesc(Long accountId, LocalDateTime date);

}
