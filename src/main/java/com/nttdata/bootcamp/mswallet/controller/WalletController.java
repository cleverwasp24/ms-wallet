package com.nttdata.bootcamp.mswallet.controller;

import com.nttdata.bootcamp.mswallet.dto.*;
import com.nttdata.bootcamp.mswallet.model.Wallet;
import com.nttdata.bootcamp.mswallet.model.Transaction;
import com.nttdata.bootcamp.mswallet.service.impl.WalletServiceImpl;
import com.nttdata.bootcamp.mswallet.service.impl.TransactionServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/bootcamp/wallet")
public class WalletController {

    @Autowired
    WalletServiceImpl walletService;

    @Autowired
    TransactionServiceImpl transactionService;

    @GetMapping(value = "/findAllWallets")
    @ResponseBody
    public Flux<Wallet> findAllWallets() {
        return walletService.findAll();
    }

    @GetMapping(value = "/findAllWalletsByClientId/{id}")
    @ResponseBody
    public Flux<Wallet> findAllWalletsByClientId(@PathVariable Long id) {
        return walletService.findAllByClientId(id);
    }

    @PostMapping(value = "/createSavingsWallet")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createSavingsWallet(@RequestBody WalletDTO savingsWalletDTO) {
        return walletService.createWallet(savingsWalletDTO);
    }

    @GetMapping(value = "/find/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Wallet>> findWalletById(@PathVariable Long id) {
        return walletService.findById(id)
                .map(wallet -> ResponseEntity.ok().body(wallet))
                .onErrorResume(e -> {
                    log.info("Wallet not found " + id, e);
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/update/{id}")
    @ResponseBody
    public Mono<ResponseEntity<Wallet>> updateWallet(@PathVariable Long id, @RequestBody Wallet wallet) {
        return walletService.update(id, wallet)
                .map(a -> new ResponseEntity<>(a, HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(value = "/delete/{id}")
    @ResponseBody
    public Mono<Void> deleteByIdWallet(@PathVariable Long id) {
        return walletService.delete(id);
    }

    @GetMapping(value = "/findAllByClientId/{id}")
    @ResponseBody
    public Flux<Wallet> findAllByClientId(@PathVariable Long id) {
        return walletService.findAllByClientId(id);
    }

    @GetMapping(value = "/getDailyBalanceReportCurrentMonth/{id}")
    @ResponseBody
    public Mono<WalletReportDTO> getDailyBalanceReportCurrentMonth(@PathVariable Long id) {
        return walletService.findById(id)
                .flatMap(wallet -> transactionService.generateWalletReportCurrentMonth(wallet.getId()))
                .switchIfEmpty(Mono.error(new Exception("Wallet not found")));
    }

    @GetMapping(value = "/getDailyBalanceReport/{id}")
    @ResponseBody
    public Mono<WalletReportDTO> getDailyBalanceReport(@PathVariable Long id, @RequestBody PeriodDTO periodDTO) {
        return walletService.findById(id)
                .flatMap(wallet -> transactionService.generateWalletReport(wallet.getId(), periodDTO))
                .switchIfEmpty(Mono.error(new Exception("Wallet not found")));
    }

    @GetMapping(value = "/getLatestTenTransactions/{id}")
    @ResponseBody
    public Flux<Transaction> getLatestTenTransactions(@PathVariable Long id) {
        return walletService.findById(id)
                .flatMapMany(wallet -> transactionService.findAllByWalletIdDesc(wallet.getId()).take(10))
                .switchIfEmpty(Mono.error(new Exception("Wallet not found")));
    }

    @GetMapping(value = "/getCompleteReport/{id}")
    @ResponseBody
    public Mono<CompleteReportDTO> getCompleteReport(@PathVariable Long id, @RequestBody PeriodDTO periodDTO) {
        return walletService.findById(id)
                .flatMap(wallet -> transactionService.generateCompleteReport(wallet.getId(), periodDTO))
                .switchIfEmpty(Mono.error(new Exception("Wallet not found")));
    }

}
