package com.nttdata.bootcamp.mswallet.service.impl;

import com.nttdata.bootcamp.mswallet.dto.DebitCardDTO;
import com.nttdata.bootcamp.mswallet.service.DebitCardService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class DebitCardServiceImpl implements DebitCardService {

    private final WebClient webClient;

    public DebitCardServiceImpl(WebClient.Builder webClientBuilder) {
        //microservicio cards
        this.webClient = webClientBuilder.baseUrl("http://localhost:8084").build();
    }

    @CircuitBreaker(name = "service-debit-card", fallbackMethod = "findByIdFallback")
    @TimeLimiter(name = "service-debit-card")
    @Override
    public Mono<DebitCardDTO> findById(Long id) {
        return this.webClient.get()
                .uri("/bootcamp/card/find/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .bodyToMono(DebitCardDTO.class);
    }

    public Flux<DebitCardDTO> findByIdFallback(Long id, Throwable t) {
        log.info("Fallback method for findByIdFallback (DEBIT CARD) executed {}", t.getMessage());
        return Flux.empty();
    }

}
