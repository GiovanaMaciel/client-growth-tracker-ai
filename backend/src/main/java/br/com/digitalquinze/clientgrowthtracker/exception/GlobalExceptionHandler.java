package br.com.digitalquinze.clientgrowthtracker.exception;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodAlreadyExistsException;
import br.com.digitalquinze.clientgrowthtracker.reportingperiod.ReportingPeriodNotFoundException;
import br.com.digitalquinze.clientgrowthtracker.restaurant.RestaurantNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================
    // 404 - RESTAURANT NOT FOUND
    // =========================================================

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRestaurantNotFound(
            RestaurantNotFoundException exception) {

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    // =========================================================
    // 404 - REPORTING PERIOD NOT FOUND
    // =========================================================

    @ExceptionHandler(ReportingPeriodNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReportingPeriodNotFound(
            ReportingPeriodNotFoundException exception) {

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    // =========================================================
    // 409 - REPORTING PERIOD ALREADY EXISTS
    // =========================================================

    @ExceptionHandler(ReportingPeriodAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleReportingPeriodAlreadyExists(
            ReportingPeriodAlreadyExistsException exception) {

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }


    // =========================================================
    // 403 - ACCESS DENIED
    // =========================================================

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception) {

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", exception.getMessage());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }


    // =========================================================
    // 400 - VALIDATION ERRORS
    //
    // Exemplo:
    //
    // @NotBlank
    // @Size
    // @Min
    // @Max
    // @Email
    // =========================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException exception) {

        Map<String, String> fields = new HashMap<>();

        exception
                .getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                    fields.put(
                        error.getField(),
                        error.getDefaultMessage()
                    )
                );

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation failed");
        body.put("fields", fields);

        return ResponseEntity
                .badRequest()
                .body(body);
    }


    // =========================================================
    // 400 - BUSINESS VALIDATION / INVALID ARGUMENT
    //
    // Examples:
    //
    // - referenceMonth is not the first day of the month
    // - email already registered
    // - RESTAURANT_USER with no restaurant
    // - AGENCY_ADMIN associated with a restaurant
    // =========================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {

        Map<String, Object> body = new HashMap<>();

        body.put("timestamp", OffsetDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", exception.getMessage());

        return ResponseEntity
                .badRequest()
                .body(body);
    }
}