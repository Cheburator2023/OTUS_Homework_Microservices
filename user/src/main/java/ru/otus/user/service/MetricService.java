package ru.otus.user.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MetricService {
    private final MeterRegistry meterRegistry;

    // Кэш для счётчиков, чтобы не создавать новые при каждом вызове
    private final ConcurrentHashMap<String, Counter> apiCounters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> errorCounters = new ConcurrentHashMap<>();

    public Counter buildApiCounter(String method, String statusCode) {
        String key = method + ":" + statusCode;
        return apiCounters.computeIfAbsent(key, k ->
                Counter.builder("user_api_calls")
                        .tag("method", method)
                        .tag("status_code", statusCode)
                        .description("Total number of " + method + " calls")
                        .register(meterRegistry)
        );
    }

    public Counter buildErrorCounter(String method, String statusCode) {
        String key = method + ":" + statusCode;
        return errorCounters.computeIfAbsent(key, k ->
                Counter.builder("user_api_errors")
                        .tag("method", method)
                        .tag("status_code", statusCode)
                        .description("Number of API errors for " + method)
                        .register(meterRegistry)
        );
    }
}