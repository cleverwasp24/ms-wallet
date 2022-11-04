package com.nttdata.bootcamp.mswallet.service.impl;

import com.nttdata.bootcamp.mswallet.infrastructure.DatabaseSequenceRepository;
import com.nttdata.bootcamp.mswallet.model.DatabaseSequence;
import com.nttdata.bootcamp.mswallet.service.DatabaseSequenceService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class DatabaseSequenceServiceImpl implements DatabaseSequenceService {

    @Autowired
    DatabaseSequenceRepository databaseSequenceRepository;

    @Override
    public Mono<Long> generateSequence(String seq) {
        return databaseSequenceRepository.findDatabaseSequenceById(seq).flatMap(sequence -> {
            sequence.setSeq(sequence.getSeq() + 1);
            return databaseSequenceRepository.save(sequence).flatMap(s -> Mono.just(s.getSeq()));
        }).switchIfEmpty(databaseSequenceRepository.save(new DatabaseSequence(seq, 1L))
                .then(Mono.just(1L)));
    }
}