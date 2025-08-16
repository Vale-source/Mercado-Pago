package inncome.modulo_mp.exception;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    //Error interno (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        LOGGER.error("Error interno inesperado", ex);
        Map<String, Object> resp = Map.of(
                "error", "ERROR_INTERNO",
                "message", ex.getMessage(),
                "type", ex.getClass().getSimpleName(),
                "Stack Trace", ex.getStackTrace()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(resp);
    }

    //Error específico de Mercado Pago API
    @ExceptionHandler({MPApiException.class})
    public ResponseEntity<Map<String, Object>> handleMercadoPagoApiException(MPApiException ex) {
        LOGGER.error("Error de Mercado Pago", ex);
        Map<String, Object> resp = Map.of(
                "error", "MERCADO_PAGO_ERROR",
                "message", ex.getMessage(),
                "type", ex.getClass().getSimpleName(),
                "Stack Trace", ex.getStackTrace(),
                "status code", ex.getStatusCode()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resp);
    }

    //Error general de Mercado Pago
    @ExceptionHandler({MPException.class})
    public ResponseEntity<Map<String, Object>> handleMercadoPagoException(MPException ex) {
        LOGGER.error("Error de Mercado Pago", ex);
        Map<String, Object> resp = Map.of(
                "error", "MERCADO_PAGO_ERROR",
                "message", ex.getMessage(),
                "type", ex.getClass().getSimpleName(),
                "Stack Trace", ex.getStackTrace()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resp);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}

