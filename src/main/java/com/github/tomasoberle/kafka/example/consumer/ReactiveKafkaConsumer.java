package com.github.tomasoberle.kafka.example.consumer;

import com.github.tomasoberle.kafka.example.service.ProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;

@Component
@ConditionalOnProperty(value = "kafka-consumer-type", havingValue = "REACTIVE")
public class ReactiveKafkaConsumer implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(ReactiveKafkaConsumer.class);

    private final ProcessingService processingService;
    private final ReactiveKafkaConsumerTemplate<String, String> consumerTemplate;
    private Disposable consumer;

    public ReactiveKafkaConsumer(ProcessingService processingService,
                                 ReactiveKafkaConsumerTemplate<String, String> consumerTemplate) {
        this.processingService = processingService;
        this.consumerTemplate = consumerTemplate;
    }

    private Disposable createConsumer() {
        return consumerTemplate.receive()
                .subscribeOn(Schedulers.newSingle("kafka-consumer")) //boundedElastic
                .doOnNext(receiverRecord -> {
                    String value = receiverRecord.value();
                    log.info("Consuming message in Reactive consumer: {}", value);
                    processingService.process(value); // BREAKPOINT (CommitFailedException) - timeout
                    // .acknowledge() OR .commit().subscribe()
                    receiverRecord.receiverOffset().acknowledge();
                })
                .onErrorContinue((throwable, o) -> log.error("Consumer error (onErrorContinue)", throwable))
                .subscribe();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (consumer == null || consumer.isDisposed()) {
            consumer = createConsumer();
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info("A 'destroy()' has been called.");
        if(consumer != null && !consumer.isDisposed()) {
            log.info("Destroying consumer - calling 'dispose()' on consumer.");
            consumer.dispose();
        }
    }
}
