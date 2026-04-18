package ru.otus.stock.dto;

public record StockResponse(
        boolean reserved,
        String message
) {}