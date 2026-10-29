package com.pulsepass.pulsepass.exception;

import com.pulsepass.pulsepass.dto.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException exception){
        
        Map<String, String> details = new TreeMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error ->
                details.merge(
                        error.getField(),
                        error.getDefaultMessage(),
                        (first, next) ->
                                first.compareTo(next) <= 0 ? first : next
                )
        );

        return response(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                details
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> malformedBody(HttpMessageNotReadableException exception) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON or invalid request body",
                Map.of()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> missingParameter(MissingServletRequestParameterException exception) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                Map.of(
                        exception.getParameterName(),
                        "Parameter is required"
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> invalidParameter(MethodArgumentTypeMismatchException exception) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                Map.of(
                        exception.getName(),
                        "Invalid parameter format"
                )
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException exception) {
        return response(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                Map.of()
        );
    }

    @ExceptionHandler({DuplicateResourceException.class,BusinessRuleException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException exception) {
        return response(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                Map.of()
        );
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> unauthenticated(Exception e){
        return response(HttpStatus.UNAUTHORIZED,"Credenciales inválidas o autenticación requerida",Map.of());
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> denied(Exception e){
        log.info("security_denied status=403 requestId={}",org.slf4j.MDC.get("requestId"));
        return response(HttpStatus.FORBIDDEN,"No tiene permisos para realizar esta operación",Map.of());
    }
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity(Exception e){return response(HttpStatus.CONFLICT,"Conflicto de datos",Map.of());}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unexpected(Exception exception) {
        // Conserva los códigos y headers de los errores de Spring MVC.
        if (exception instanceof org.springframework.web.ErrorResponse frameworkError) {
            HttpStatus status = HttpStatus.valueOf(frameworkError.getStatusCode().value());

            return ResponseEntity.status(status)
                    .headers(frameworkError.getHeaders())
                    .body(new ErrorResponse(
                            LocalDateTime.now(),
                            status.value(),
                            status.getReasonPhrase(),
                            status.getReasonPhrase(),
                            Map.of()
                    ));
        }

        log.error("Unexpected API error", exception);

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                Map.of()
        );
    }

    private ResponseEntity<ErrorResponse> response(HttpStatus status,String message,Map<String, String> details){

        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        details
                ));
    }
}