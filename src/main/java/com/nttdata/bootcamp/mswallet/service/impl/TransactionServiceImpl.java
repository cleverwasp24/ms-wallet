package com.nttdata.bootcamp.mswallet.service.impl;

import com.nttdata.bootcamp.mswallet.dto.*;
import com.nttdata.bootcamp.mswallet.infrastructure.TransactionRepository;
import com.nttdata.bootcamp.mswallet.mapper.TransactionDTOMapper;
import com.nttdata.bootcamp.mswallet.model.Wallet;
import com.nttdata.bootcamp.mswallet.model.Transaction;
import com.nttdata.bootcamp.mswallet.model.enums.TransactionTypeEnum;
import com.nttdata.bootcamp.mswallet.service.DatabaseSequenceService;
import com.nttdata.bootcamp.mswallet.service.TransactionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

@Log4j2
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletServiceImpl walletService;

    @Autowired
    private DatabaseSequenceService databaseSequenceService;

    private TransactionDTOMapper transactionDTOMapper = new TransactionDTOMapper();

    @Override
    public Flux<Transaction> findAll() {
        log.info("Listing all transactions");
        return transactionRepository.findAll();
    }

    @Override
    public Mono<Transaction> create(Transaction transaction) {
        log.info("Creating transaction: " + transaction.toString());
        return transactionRepository.save(transaction);
    }

    @Override
    public Mono<Transaction> findById(Long id) {
        log.info("Searching transaction by id: " + id);
        return transactionRepository.findById(id);
    }

    @Override
    public Mono<Transaction> update(Long id, Transaction transaction) {
        log.info("Updating transaction with id: " + id + " with : " + transaction.toString());
        return transactionRepository.findById(id).flatMap(a -> {
            transaction.setId(id);
            return transactionRepository.save(transaction);
        });
    }

    @Override
    public Mono<Void> delete(Long id) {
        log.info("Deleting transaction with id: " + id);
        return transactionRepository.deleteById(id);
    }

    /**
     * This method makes a deposit in a wallet
     *
     * @param transactionDTO
     * @return
     */
    @Override
    public Mono<String> deposit(TransactionDTO transactionDTO) {
        log.info("Making a deposit: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.DEPOSIT);
        //Validar los datos de la transaccion
        return checkFields(transaction)
                //Si los datos son correctos, validar que el monedero exista
                .switchIfEmpty(walletService.findById(transaction.getWalletId()).flatMap(a -> {
                    //Se actualiza el saldo del monedero
                    a.setBalance(a.getBalance() + transaction.getAmount());
                    transaction.setNewBalance(a.getBalance());
                    //Se actualiza el monedero con el nuevo saldo
                    return walletService.update(a.getId(), a)
                            //Se genera el ID de la transacción y se crea la transacción
                            .flatMap(ac -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                transaction.setId(id);
                                return transactionRepository.save(transaction);
                            }))
                            .flatMap(t -> Mono.just("Deposit done, new balance: " + a.getBalance()));

                    //Si el monedero no existe, se cancela la transacción
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Wallet not found"))));
    }

    /**
     * This method makes a withdrawal from a wallet
     *
     * @param transactionDTO
     * @return
     */
    @Override
    public Mono<String> withdraw(TransactionDTO transactionDTO) {
        log.info("Making a withdraw: " + transactionDTO.toString());
        Transaction transaction = transactionDTOMapper.convertToEntity(transactionDTO, TransactionTypeEnum.WITHDRAW);
        //Validar los datos de la transaccion
        return checkFields(transaction)
                //Si los datos son correctos, validar que el monedero exista
                .switchIfEmpty(walletService.findById(transaction.getWalletId()).flatMap(a -> {
                    //Se actualiza el saldo del monedero
                    a.setBalance(a.getBalance() - transaction.getAmount());
                    if (a.getBalance() < 0) {
                        return Mono.error(new IllegalArgumentException("Insufficient balance to withdraw"));
                    }
                    transaction.setNewBalance(a.getBalance());
                    //Se actualiza el monedero con el nuevo saldo
                    return walletService.update(a.getId(), a)
                            //Se genera el ID de la transacción y se crea la transacción
                            .flatMap(ac -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                transaction.setId(id);
                                return transactionRepository.save(transaction);
                            }))
                            .flatMap(t -> Mono.just("Withdraw done, new balance: " + a.getBalance()));
                    //Si el monedero no existe, se cancela la transacción
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Wallet not found"))));
    }

    /**
     * This method transfers money from one wallet to another
     *
     * @param transferDTO
     * @return
     */
    @Override
    public Mono<String> transfer(TransferDTO transferDTO) {
        log.info("Third Wallet transfer: " + transferDTO.toString() + " destination wallet: " + transferDTO.getDestinationWalletId());
        Transaction transaction = transactionDTOMapper.convertToEntity(transferDTO, TransactionTypeEnum.TRANSFER);
        //Validar los datos de la transferencia
        return checkFields(transaction)
                //Si los datos son correctos, validar que el monedero origen exista
                .switchIfEmpty(walletService.findById(transaction.getWalletId()).flatMap(originWallet -> {
                    //Si el monedero origen existe, verificar que el monedero origen tenga el saldo suficiente para realizar la transferencia
                    return walletService.findById(transaction.getDestinationWalletId()).flatMap(destinationWallet -> {
                        originWallet.setBalance(originWallet.getBalance() - transaction.getAmount());
                        if (originWallet.getBalance() < 0) {
                            return Mono.error(new IllegalArgumentException("Insufficient balance to transfer"));
                        }
                        transaction.setNewBalance(originWallet.getBalance());
                        destinationWallet.setBalance(destinationWallet.getBalance() + transaction.getAmount());
                        //Crear una transacción en el monedero destino
                        Transaction destinationWalletTransaction = transactionDTOMapper.generateDestinationWalletTransaction(transaction);
                        destinationWalletTransaction.setNewBalance(destinationWallet.getBalance());
                        //Actualizar monedero origen
                        return walletService.update(originWallet.getId(), originWallet)
                                //Generar id de la transacción y registrar la transacción en el monedero origen
                                .flatMap(oa -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    transaction.setId(id);
                                    return transactionRepository.save(transaction);
                                }))
                                //Actualizar monedero destino
                                .flatMap(ot -> walletService.update(destinationWallet.getId(), destinationWallet))
                                //Generar id de la transacción y registrar la transacción en el monedero destino
                                .flatMap(da -> databaseSequenceService.generateSequence(Transaction.SEQUENCE_NAME).flatMap(id -> {
                                    destinationWalletTransaction.setId(id);
                                    return transactionRepository.save(destinationWalletTransaction);
                                }))
                                .flatMap(dt -> Mono.just("Transfer to third wallet done, new balance: " + originWallet.getBalance()));
                        //Si el monedero destino no existe, se cancela la transacción
                    }).switchIfEmpty(Mono.error(new IllegalArgumentException("Destination wallet not found")));
                    //Si el monedero origen no existe, se cancela la transacción
                }).switchIfEmpty(Mono.error(new IllegalArgumentException("Origin Wallet not found"))));
    }

    /**
     * This method finds all transactions by wallet id
     *
     * @param walletId
     * @return
     */
    @Override
    public Flux<Transaction> findAllByWalletId(Long walletId) {
        log.info("Listing all transactions by wallet id");
        return transactionRepository.findAllByWalletId(walletId);
    }

    /**
     * This method finds all transactions by wallet id in descending order by date
     *
     * @param walletId
     * @return
     */
    @Override
    public Flux<Transaction> findAllByWalletIdDesc(Long walletId) {
        log.info("Listing all transactions by wallet id order by date desc");
        return transactionRepository.findAllByWalletIdOrderByTransactionDateDesc(walletId);
    }

    /**
     * This method finds all transactions by wallet id in the current month
     *
     * @param walletId
     * @param date
     * @return
     */
    @Override
    public Flux<Transaction> findTransactionsWalletMonth(Long walletId, LocalDateTime date) {
        return transactionRepository.findAllByWalletIdAndTransactionDateBetween(walletId,
                date.withDayOfMonth(1).with(LocalTime.MIN), date.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX));
    }

    /**
     * This method finds all transactions by wallet id in a range of dates
     *
     * @param walletId
     * @param start
     * @param end
     * @return
     */
    @Override
    public Flux<Transaction> findTransactionsWalletPeriod(Long walletId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findAllByWalletIdAndTransactionDateBetween(walletId, start, end);
    }

    /**
     * This method validates the fields of the transaction
     *
     * @param transaction
     * @return
     */
    @Override
    public Mono<String> checkFields(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
            return Mono.error(new IllegalArgumentException("Wallet transaction amount must be greater than 0"));
        }
        return Mono.empty();
    }

    /**
     * This method generates a complete report of the transactions of a wallet
     *
     * @param id
     * @param periodDTO
     * @return
     */
    @Override
    public Mono<CompleteReportDTO> generateCompleteReport(Long id, PeriodDTO periodDTO) {
        log.info("Generating complete report in a period: " + periodDTO.getStart() + " - " + periodDTO.getEnd());
        Mono<CompleteReportDTO> completeReportDTOMono = Mono.just(new CompleteReportDTO());
        Mono<Wallet> walletMono = walletService.findById(id);
        Flux<Transaction> transactionFlux = findTransactionsWalletPeriod(id, periodDTO.getStart(), periodDTO.getEnd());
        return completeReportDTOMono.flatMap(r -> walletMono.map(wallet -> {
            r.setWallet(wallet);
            return r;
        }).flatMap(r2 -> transactionFlux.collectList().map(transactions -> {
            r2.setTransactions(transactions);
            return r2;
        })));
    }

    /**
     * This method finds the last transaction of a wallet made before a specific date
     * Este metodo encuentra la última transacción realizada en un monedero antes de una fecha
     *
     * @param id
     * @param date
     * @return
     */
    @Override
    public Mono<Transaction> findLastTransactionBefore(Long id, LocalDateTime date) {
        return transactionRepository.findByWalletIdAndTransactionDateBeforeOrderByTransactionDateDesc(id, date)
                .flatMap(t -> Mono.just(t))
                //if it is empty take the wallet opening balance and creation date
                .switchIfEmpty(walletService.findById(id).flatMap(a -> {
                    Transaction transaction = new Transaction();
                    transaction.setNewBalance(a.getInitialBalance());
                    transaction.setTransactionDate(a.getCreationDate());
                    return Mono.just(transaction);
                }));
    }

    @Override
    public Mono<WalletReportDTO> generateWalletReportCurrentMonth(Long id) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.withDayOfMonth(1).with(LocalTime.MIN);
        log.info("Generating wallet report for current month: " + start + " - " + now);
        return generateWalletReport(id, new PeriodDTO(start, now));
    }

    @Override
    public Mono<WalletReportDTO> generateWalletReport(Long id, PeriodDTO periodDTO) {
        log.info("Generating wallet report in a period: " + periodDTO.getStart() + " - " + periodDTO.getEnd());
        Mono<WalletReportDTO> walletReportDTOMono = Mono.just(new WalletReportDTO());
        Mono<Wallet> walletMono = walletService.findById(id);
        Mono<Transaction> firstBefore = findLastTransactionBefore(id, periodDTO.getStart());
        Flux<Transaction> transactionFlux = findTransactionsWalletPeriod(id, periodDTO.getStart(), periodDTO.getEnd());
        return walletReportDTOMono.flatMap(r -> walletMono.map(wallet -> {
                    r.setWallet(wallet);
                    return r;
                }))
                .flatMap(r -> transactionFlux.collectList().map(tl -> {
                    tl = tl.stream().collect(
                                    Collectors.groupingBy(t -> t.getTransactionDate().toLocalDate(),
                                            Collectors.collectingAndThen(
                                                    Collectors.maxBy(
                                                            Comparator.comparing(Transaction::getTransactionDate)),
                                                    transaction -> transaction.get())))
                            .values().stream().collect(Collectors.toList());
                    //Add all transactions to the report as daily balances
                    tl.forEach(t -> r.getDailyBalances().add(new DailyBalanceDTO(t.getTransactionDate().toLocalDate(), t.getNewBalance())));
                    return r;
                }))
                .flatMap(r -> firstBefore.map(t -> {
                    //If transaction list does not contain a transaction on the start date, add it
                    if (r.getDailyBalances().stream().noneMatch(ta -> ta.getDate().equals(periodDTO.getStart().toLocalDate()))) {
                        if (t.getTransactionDate().toLocalDate().equals(periodDTO.getStart().toLocalDate())) {
                            r.getDailyBalances().add(new DailyBalanceDTO(t.getTransactionDate().toLocalDate(), t.getNewBalance()));
                        } else {
                            r.getDailyBalances().add(new DailyBalanceDTO(periodDTO.getStart().toLocalDate(), 0.00));
                        }
                    }
                    return r;
                }))
                //Fill missingDays in the transaction list
                .flatMap(r -> {
                    long days = ChronoUnit.DAYS.between(periodDTO.getStart().toLocalDate(), periodDTO.getEnd().toLocalDate());
                    HashMap<LocalDate, Double> map = new HashMap<>();
                    r.getDailyBalances().forEach(t -> map.put(t.getDate(), t.getBalance()));
                    for (int i = 1; i <= days; i++) {
                        LocalDate date = periodDTO.getStart().toLocalDate().plusDays(i);
                        if (!map.containsKey(date)) {
                            map.put(date, map.get(date.minusDays(1)));
                        }
                    }
                    r.setDailyBalances(new ArrayList<>());
                    map.forEach((k, v) -> r.getDailyBalances().add(new DailyBalanceDTO(k, v)));
                    //Sort the list by date
                    r.getDailyBalances().sort(Comparator.comparing(DailyBalanceDTO::getDate));
                    return Mono.just(r);
                });
    }

}
