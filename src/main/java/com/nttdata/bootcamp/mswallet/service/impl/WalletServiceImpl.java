package com.nttdata.bootcamp.mswallet.service.impl;

import com.nttdata.bootcamp.mswallet.dto.WalletDTO;
import com.nttdata.bootcamp.mswallet.infrastructure.WalletRepository;
import com.nttdata.bootcamp.mswallet.mapper.WalletDTOMapper;
import com.nttdata.bootcamp.mswallet.model.Wallet;
import com.nttdata.bootcamp.mswallet.service.DebitCardService;
import com.nttdata.bootcamp.mswallet.service.WalletService;
import com.nttdata.bootcamp.mswallet.service.DatabaseSequenceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ClientServiceImpl clientService;

    @Autowired
    private DebitCardService debitCardService;

    @Autowired
    private DatabaseSequenceService databaseSequenceService;

    private WalletDTOMapper walletDTOMapper = new WalletDTOMapper();

    @Override
    public Flux<Wallet> findAll() {
        log.info("Listing all wallets");
        return walletRepository.findAll();
    }

    @Override
    public Mono<Wallet> create(Wallet wallet) {
        log.info("Creating wallet: " + wallet.toString());
        return walletRepository.save(wallet);
    }

    @Override
    public Mono<Wallet> findById(Long id) {
        log.info("Searching wallet by id: " + id);
        return walletRepository.findById(id);
    }

    @Override
    public Mono<Wallet> update(Long id, Wallet wallet) {
        log.info("Updating wallet with id: " + id + " with : " + wallet.toString());
        return walletRepository.findById(id).flatMap(a -> {
            wallet.setId(id);
            return walletRepository.save(wallet);
        });
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting wallet with id: " + id);
        return walletRepository.deleteById(id);
    }

    @Override
    public Mono<String> createWallet(WalletDTO walletDTO) {
        log.info("Creating wallet: " + walletDTO.toString());
        Wallet wallet = walletDTOMapper.convertToEntity(walletDTO);
        //Validar los datos del monedero
        return checkFields(wallet)
                //Validar que el cliente exista
                .switchIfEmpty(clientService.findById(wallet.getClientId()).flatMap(c -> {
                    return databaseSequenceService.generateSequence(Wallet.SEQUENCE_NAME).flatMap(sequence -> {
                        wallet.setId(sequence);
                        //Si asigna una tarjeta de debito, validar que exista
                        if (wallet.getDebitCardId() != null && wallet.getDebitCardId() > 0) {
                            return debitCardService.findById(wallet.getDebitCardId())
                                    .flatMap(d -> {
                                        //Verificar que la tarjeta de debito pertenezca al cliente
                                        if (d.getClientId().equals(wallet.getClientId())) {
                                            return walletRepository.save(wallet).flatMap(w -> Mono.just("Wallet created! " + walletDTOMapper.convertToDto(w)));
                                        } else {
                                            return Mono.error(new Exception("The debit card does not belong to the client"));
                                        }
                                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Debit card not found")));
                        } else {
                            wallet.setDebitCardId(null);
                            return walletRepository.save(wallet)
                                    .flatMap(w -> Mono.just("Wallet created! " + walletDTOMapper.convertToDto(w)));
                        }
                    });
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Client not found"))));
    }

    @Override
    public Flux<Wallet> findAllByClientId(Long id) {
        log.info("Listing all wallets by client id");
        return walletRepository.findAllByClientId(id);
    }

    @Override
    public Mono<String> checkFields(Wallet wallet) {
        if (wallet.getWalletNumber() == null || wallet.getWalletNumber().trim().equals("")) {
            return Mono.error(new IllegalArgumentException("Wallet number cannot be empty"));
        }
        if (wallet.getBalance() == null || wallet.getBalance() < 0) {
            return Mono.error(new IllegalArgumentException("New wallet balance must be equal or greater than 0"));
        }
        return Mono.empty();
    }

}
