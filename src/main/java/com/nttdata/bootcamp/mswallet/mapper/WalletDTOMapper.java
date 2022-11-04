package com.nttdata.bootcamp.mswallet.mapper;

import com.nttdata.bootcamp.mswallet.dto.*;
import com.nttdata.bootcamp.mswallet.model.Wallet;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class WalletDTOMapper {
    
    @Autowired
    private ModelMapper modelMapper = new ModelMapper();

    public Wallet convertToDto(Wallet wallet) {
        return modelMapper.map(wallet, Wallet.class);
    }
    public Wallet convertToEntity(WalletDTO walletDTO) {
        Wallet wallet = modelMapper.map(walletDTO, Wallet.class);
        wallet.setInitialBalance(wallet.getBalance());
        wallet.setCreationDate(LocalDateTime.now());
        return wallet;
    }
    
}
