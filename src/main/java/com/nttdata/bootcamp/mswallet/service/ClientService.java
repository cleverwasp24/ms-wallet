package com.nttdata.bootcamp.mswallet.service;

import com.nttdata.bootcamp.mswallet.dto.ClientDTO;
import reactor.core.publisher.Mono;

public interface ClientService {

    Mono<ClientDTO> findById(Long id);

}
