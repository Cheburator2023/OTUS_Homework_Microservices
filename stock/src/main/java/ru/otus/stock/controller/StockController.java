package ru.otus.stock.controller;

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
import ru.otus.stock.dto.ErrorResponse;
import ru.otus.stock.dto.ReleaseRequest;
import ru.otus.stock.dto.ReserveRequest;
import ru.otus.stock.dto.StockResponse;
import ru.otus.stock.service.StockService;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock API")
public class StockController {

    private final StockService stockService;

    @PostMapping("/reserve")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reserve product stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock reserved successfully",
                    content = @Content(schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient stock",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public StockResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return stockService.reserve(request);
    }

    @PostMapping("/release")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Release reserved product stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock released successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void release(@Valid @RequestBody ReleaseRequest request) {
        stockService.release(request);
    }
}