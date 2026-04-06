package ru.otus.delivery.exception;

public class DeliverySlotNotFoundException extends RuntimeException {
    public DeliverySlotNotFoundException(String message) { super(message); }
}