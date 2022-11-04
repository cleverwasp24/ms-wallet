package com.nttdata.bootcamp.mswallet.service;

import reactor.core.publisher.Mono;

public interface DatabaseSequenceService {

    Mono<Long> generateSequence(String seqName);

}
