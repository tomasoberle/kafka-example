package com.github.tomasoberle.kafka.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ProcessingServiceImpl implements ProcessingService {
    private static final Logger log = LoggerFactory.getLogger(ProcessingServiceImpl.class);

    @Override
    public void process(String value) {
        if (value.startsWith("ERROR")) {
            simulateErrorDuringProcessing(value);
        } else {
            simulateLongProcessing(value);
        }
    }

    private void simulateErrorDuringProcessing(String value) {
        log.info("Processing will fail with error (exception)...");
        throw new RuntimeException("Processing failed with exception for: " + value);
    }

    private void simulateLongProcessing(String value) {
        log.info("Consumer processing started: {}", value);
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Consumer processing successfully finished: {}", value);
    }
}
