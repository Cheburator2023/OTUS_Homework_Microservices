package ru.otus.delivery.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.otus.delivery.dto.DeliveryResponse;
import ru.otus.delivery.dto.ErrorResponse;
import ru.otus.delivery.dto.ReleaseDeliveryRequest;
import ru.otus.delivery.dto.ReserveDeliveryRequest;
import ru.otus.delivery.service.DeliveryService;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
@Tag(name = "Delivery API")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reserve delivery slot for an order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot reserved successfully",
                    content = @Content(schema = @Schema(implementation = DeliveryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Delivery slot not found for given time",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slot already reserved",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public DeliveryResponse reserve(@Valid @RequestBody ReserveDeliveryRequest request) {
        return deliveryService.reserve(request);
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Release previously reserved delivery slot")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slot released successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No reservation found for given orderId",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void release(@Valid @RequestBody ReleaseDeliveryRequest request) {
        deliveryService.release(request);
    }
}