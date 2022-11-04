package com.nttdata.bootcamp.mswallet.service;

import com.nttdata.bootcamp.mswallet.dto.WalletDTO;
import com.nttdata.bootcamp.mswallet.model.Wallet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WalletService {

    Flux<Wallet> findAll();

    Mono<Wallet> create(Wallet wallet);

    Mono<Wallet> findById(Long id);

    Mono<Wallet> update(Long id, Wallet wallet);

    Mono<Void> delete(Long id);

    Mono<String> createWallet(WalletDTO walletDTO);

    Flux<Wallet> findAllByClientId(Long id);

    Mono<String> checkFields(Wallet wallet);

}
