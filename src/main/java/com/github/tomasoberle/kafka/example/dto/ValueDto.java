package com.github.tomasoberle.kafka.example.dto;

public class ValueDto {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ValueDto{" +
                "value='" + value + '\'' +
                '}';
    }
}
