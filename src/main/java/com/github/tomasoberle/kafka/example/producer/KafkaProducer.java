package com.github.tomasoberle.kafka.example.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class KafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

    private static final String TOPIC = "test-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping(path = "/send", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> send() {
        String message = LocalDateTime.now().toString();
        log.info("Sending message '{}' to topic '{}'", message, TOPIC);
        kafkaTemplate.send(TOPIC, message);
        return ResponseEntity.ok("OK\n");
    }

    @PostMapping(path = "/send/nack", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sendWithNackFlag() {
        String message = "NACK " + LocalDateTime.now();
        log.info("Sending message with NACK flag '{}' to topic '{}'", message, TOPIC);
        kafkaTemplate.send(TOPIC, message);
        return ResponseEntity.ok("OK\n");
    }

    @PostMapping(path = "/send/error", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sendError() {
        String message = "ERROR " + LocalDateTime.now();
        log.info("Sending message with error '{}' to topic '{}'", message, TOPIC);
        kafkaTemplate.send(TOPIC, message);
        return ResponseEntity.ok("OK\n");
    }

    @PostMapping(path = "/send/error-retry", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sendErrorWithRetry() {
        String message = "ERROR_RETRY " + LocalDateTime.now();
        log.info("Sending message with error with retry '{}' to topic '{}'", message, TOPIC);
        kafkaTemplate.send(TOPIC, message);
        return ResponseEntity.ok("OK\n");
    }

    @PostMapping(path = "/send/long-processing", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sendLongProcessing() {
        String message = "LONG_PROCESSING " + LocalDateTime.now();
        log.info("Sending message with long processing flag '{}' to topic '{}'", message, TOPIC);
        kafkaTemplate.send(TOPIC, message);
        return ResponseEntity.ok("OK\n");
    }

    @PostMapping(path = "/send/count/{count}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> sendMultiple(@PathVariable int count) {
        for (int i=0; i < count; i++) {
            String message = LocalDateTime.now() + " " + i;
            log.info("Sending message '{}' to topic '{}'", message, TOPIC);
            kafkaTemplate.send(TOPIC, message);
        }
        return ResponseEntity.ok("OK\n");
    }
}
