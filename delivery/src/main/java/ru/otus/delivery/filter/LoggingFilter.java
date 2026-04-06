package ru.otus.delivery.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter extends OncePerRequestFilter {

    private final RequestMappingHandlerMapping handlerMapping;

    public LoggingFilter(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            HandlerExecutionChain chain = handlerMapping.getHandler(request);
            if (chain != null && chain.getHandler() != null) {
                log.info("Request {} {} -> handler: {}",
                        request.getMethod(),
                        request.getRequestURI(),
                        chain.getHandler().getClass().getName());
            } else {
                log.warn("No handler found for {} {}", request.getMethod(), request.getRequestURI());
            }
        } catch (Exception e) {
            log.error("Error resolving handler", e);
        }
        filterChain.doFilter(request, response);
    }
}