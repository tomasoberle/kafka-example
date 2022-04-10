package com.github.tomasoberle.kafka.example.service;

import reactor.core.publisher.Mono;

public interface ReactiveProcessingService {

    Mono<Void> process(String value);
}
