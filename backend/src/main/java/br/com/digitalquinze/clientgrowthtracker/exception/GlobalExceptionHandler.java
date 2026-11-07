package br.com.digitalquinze.clientgrowthtracker.exception;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.digitalquinze.clientgrowthtracker.channel.ChannelNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetricAlreadyExistsException;
import br.com.digitalquinze.clientgrowthtracker.channelmetric.ChannelMetricNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodAlreadyExistsException;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.RestaurantChannelAlreadyExistsException;
import br.com.digitalquinze.clientgrowthtracker.restaurantchannel.RestaurantChannelNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // 404 - NOT FOUND
    // =========================================================

    @ExceptionHandler({
        RestaurantNotFoundException.class,
        ReportingPeriodNotFoundException.class,
        ChannelNotFoundException.class,
        RestaurantChannelNotFoundException.class,
        ChannelMetricNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(
            RuntimeException exception) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                exception.getMessage()
        );
    }


    // =========================================================
    // 409 - CONFLICT
    // =========================================================

    @ExceptionHandler({
        ReportingPeriodAlreadyExistsException.class,
        RestaurantChannelAlreadyExistsException.class,
        ChannelMetricAlreadyExistsException.class
    })
    public ResponseEntity<Map<String, Object>> handleConflict(
            RuntimeException exception) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Conflict",
                exception.getMessage()
        );
    }


    // =========================================================
    // 403 - FORBIDDEN
    // =========================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception) {

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                exception.getMessage()
        );
    }


    // =========================================================
    // 400 - VALIDATION ERRORS
    //
    // Example:
    // @NotBlank
    // @NotNull
    // @Min
    // @Max
    // @DecimalMin
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> fields = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                    fields.put(
                        error.getField(),
                        error.getDefaultMessage()
                    )
                );

        Map<String, Object> body = new HashMap<>();

        body.put(
            "timestamp",
            OffsetDateTime.now()
        );

        body.put(
            "status",
            HttpStatus.BAD_REQUEST.value()
        );

        body.put(
            "error",
            "Validation failed"
        );

        body.put(
            "fields",
            fields
        );

        return ResponseEntity
                .badRequest()
                .body(body);
    }


    // =========================================================
    // 400 - BUSINESS VALIDATION
    //
    // Example:
    // - netRevenue > grossRevenue
    // - referenceMonth is not the first day of the month
    // - restaurant channel is inactive
    // - e-mail is already registered
    // =========================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                exception.getMessage()
        );
    }


    // =========================================================
    // 400 - INVALID JSON
    //
    // Example:
    // Malformed JSON or invalid enum
    // =========================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJson(
            HttpMessageNotReadableException exception) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Invalid request body"
        );
    }


    // =========================================================
    // AUXILIARY METHOD
    // =========================================================

    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String error,
            String message) {

        Map<String, Object> body = new HashMap<>();

        body.put(
            "timestamp",
            OffsetDateTime.now()
        );

        body.put(
            "status",
            status.value()
        );

        body.put(
            "error",
            error
        );

        body.put(
            "message",
            message
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}