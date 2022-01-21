package com.github.tomasoberle.kafka.example.consumer;

import com.github.tomasoberle.kafka.example.service.ProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(value = "kafka-consumer-type", havingValue = "REACTIVE_RESTARTABLE")
public class ReactiveKafkaRestartableConsumer implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(ReactiveKafkaRestartableConsumer.class);

    private final ProcessingService processingService;
    private final ReceiverOptions<String, String> receiverOptions;
    private Disposable consumer;
    private boolean consumerStopped;

    public ReactiveKafkaRestartableConsumer(ProcessingService processingService,
                                            ReceiverOptions<String, String> receiverOptions) {
        this.processingService = processingService;
        this.receiverOptions = receiverOptions;
    }

    private Disposable createConsumer() {
        receiverOptions.addAssignListener(rp -> log.info("Consumer/listener partition assigned: {}", rp))
                .addRevokeListener(receiverPartitions -> {
                    consumerStopped = true;
                    log.warn("Consumer/listener partition revoked: {}", receiverPartitions);
                });

        return KafkaReceiver.create(receiverOptions)
                .receive()
                .doOnNext(receiverRecord -> {
                    String value = receiverRecord.value();
                    log.info("Consuming message in Reactive consumer (RESTARTABLE): {}", value);
                    processingService.process(value); // BREAKPOINT (CommitFailed/Error - timeout)
                    // .acknowledge() OR .commit().subscribe()
                    receiverRecord.receiverOffset().acknowledge();
                }).doOnError(throwable -> {
                    consumerStopped = true;
                    log.error("Consumer error (doOnError)", throwable);
                }).subscribe();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Schedulers.boundedElastic().schedulePeriodically(() -> {
            log.info("Periodically checking consumer (Instance is created: '{}', stopped flag '{}').",
                    consumer == null ? "false": "true", consumerStopped);
            if (consumer == null || consumerStopped) {
                if (consumer != null) {
                    consumer.dispose();
                }
                log.info("Creating consumer...");
                consumer = createConsumer();
                consumerStopped = false;
                log.info("Consumer has been created.");
            }
        }, 0, 20, TimeUnit.SECONDS); // "MANUALLY" RESTARTABLE REACTIVE CONSUMER
    }

    @Override
    public void destroy() throws Exception {
        log.debug("A 'destroy()' has been called.");
        if (consumer != null && !consumer.isDisposed()) {
            log.debug("Destroying consumer - calling 'dispose()' on consumer.");
            consumer.dispose();
        }
    }
}
