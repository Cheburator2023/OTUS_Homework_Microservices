package ru.otus.delivery.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class EndpointDebugListener implements ApplicationListener<ApplicationReadyEvent> {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        System.out.println("=== REGISTERED ENDPOINTS ===");
        requestMappingHandlerMapping.getHandlerMethods().forEach((mapping, handler) -> {
            System.out.println(mapping + " -> " + handler);
        });
        System.out.println("=== END OF ENDPOINTS ===");
    }
}