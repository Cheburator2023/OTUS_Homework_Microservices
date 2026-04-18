package ru.otus.delivery.exception;

public class DeliverySlotNotAvailableException extends RuntimeException {
    public DeliverySlotNotAvailableException(String message) { super(message); }
}