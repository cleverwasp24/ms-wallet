package com.nttdata.bootcamp.mswallet.infrastructure;

import com.nttdata.bootcamp.mswallet.model.Wallet;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface WalletRepository extends ReactiveMongoRepository<Wallet, Long> {

    Flux<Wallet> findAllByClientId(Long id);

}
