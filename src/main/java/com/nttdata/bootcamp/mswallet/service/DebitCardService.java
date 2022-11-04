package com.nttdata.bootcamp.mswallet.service;

import com.nttdata.bootcamp.mswallet.dto.DebitCardDTO;
import reactor.core.publisher.Mono;

public interface DebitCardService {

    Mono<DebitCardDTO> findById(Long id);

}
