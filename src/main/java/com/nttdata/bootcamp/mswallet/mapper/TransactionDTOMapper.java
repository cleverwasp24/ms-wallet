package com.nttdata.bootcamp.mswallet.mapper;

import com.nttdata.bootcamp.mswallet.dto.TransactionDTO;
import com.nttdata.bootcamp.mswallet.dto.TransferDTO;
import com.nttdata.bootcamp.mswallet.model.Transaction;
import com.nttdata.bootcamp.mswallet.model.enums.TransactionTypeEnum;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class TransactionDTOMapper {

    @Autowired
    private ModelMapper modelMapper = new ModelMapper();

    public Object convertToDto(Transaction transaction, TransactionTypeEnum type) {
        return switch (type) {
            case DEPOSIT, WITHDRAW -> modelMapper.map(transaction, TransactionDTO.class);
            case TRANSFER -> modelMapper.map(transaction, TransferDTO.class);
        };
    }

    public Transaction convertToEntity(Object transactionDTO, TransactionTypeEnum type) {
        Transaction transaction = modelMapper.map(transactionDTO, Transaction.class);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setTransactionType(type.ordinal());

        switch (type) {
            case DEPOSIT -> transaction.setDescription("WALLET DEPOSIT +$ " + transaction.getAmount());
            case WITHDRAW -> transaction.setDescription("WALLET WITHDRAW -$ " + transaction.getAmount());
            case TRANSFER -> transaction.setDescription("SEND WALLET TRANSFER -$ " + transaction.getAmount());
        }

        return transaction;
    }

    public Transaction generateDestinationWalletTransaction(Transaction transaction) {
        Transaction destinationTransaction = new Transaction();
        destinationTransaction.setWalletId(transaction.getDestinationWalletId());
        destinationTransaction.setDestinationWalletId(transaction.getDestinationWalletId());
        destinationTransaction.setTransactionType(transaction.getTransactionType());
        destinationTransaction.setAmount(transaction.getAmount());
        destinationTransaction.setDescription("RECEIVE WALLET TRANSFER +$ " + transaction.getAmount());
        destinationTransaction.setTransactionDate(LocalDateTime.now());
        return destinationTransaction;
    }
}
