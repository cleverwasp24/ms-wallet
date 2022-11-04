package com.nttdata.bootcamp.mswallet.service.impl;

import com.nttdata.bootcamp.mswallet.dto.ClientDTO;
import com.nttdata.bootcamp.mswallet.service.ClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class ClientServiceImpl implements ClientService {

    private final WebClient webClient;

    public ClientServiceImpl(WebClient.Builder webClientBuilder) {
        //microservicio cliente
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    @CircuitBreaker(name = "service-client", fallbackMethod = "findByIdFallback")
    @TimeLimiter(name = "service-client")
    @Override
    public Mono<ClientDTO> findById(Long id) {
        return this.webClient.get()
                .uri("/bootcamp/client/find/{id}", id)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Error " + clientResponse.statusCode())))
                .bodyToMono(ClientDTO.class);
    }

    public Mono<ClientDTO> findByIdFallback(Long id, Throwable t) {
        log.error("Fallback method for findByIdFallback (CLIENT) executed {}", t.getMessage());
        return Mono.empty();
    }

}
