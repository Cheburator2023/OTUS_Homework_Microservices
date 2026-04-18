package ru.otus.delivery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.otus.delivery.dto.ErrorResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DeliverySlotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DeliverySlotNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("PRODUCT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(DeliverySlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(DeliverySlotNotAvailableException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("INSUFFICIENT_STOCK", ex.getMessage()));
    }
}