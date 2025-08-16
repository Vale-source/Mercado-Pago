package inncome.modulo_mp.controller;

import com.mercadopago.exceptions.MPApiException;
import inncome.modulo_mp.persistence.dto.PaymentRequestDto;
import inncome.modulo_mp.persistence.dto.PaymentResponseDto;
import inncome.modulo_mp.persistence.entity.PaymentEntity;
import inncome.modulo_mp.service.serviceImpl.PaymentServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
@Tag(name = "Gestión de Pagos", description = "Endpoints para crear y gestionar pagos con MercadoPago")
public class PaymentController {

    private final PaymentServiceImpl paymentService;

    public PaymentController(PaymentServiceImpl paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
            summary = "Crear un nuevo pago",
            description = "Genera una nueva preferencia de pago en MercadoPago usando los datos proporcionados en el DTO. " +
                    "Retorna la información del pago creado incluyendo la URL de checkout." +
                    "Los metodos pueden ser account_money, debit_card o credit_card" +
                    "Los tipos de pago deben ser account_money, debmaster, debvisa, amex, visa, master"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pago creado exitosamente. En caso de que el pago se realice usando Checkout Pro (account_money o splitPayment = true)" +
                            "el frontend debe utilizar el id que retorna el back para implementar un front (Leer la documentation de MercadoPago Checkout Pro)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de pago inválidos o faltantes",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno de MercadoPago o del servidor",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Falta Token de la tarjeta de credito/debito",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Metodo de pago o Tipo de pago incorrecto",
                    content = @Content
            )
    })
    @PostMapping()
    public ResponseEntity<PaymentResponseDto> createPayment(
            @Parameter(
                    description = "Datos del pago a crear, incluyendo información del comprador, items y configuración",
                    required = true
            )
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del pago a crear, incluyendo información del comprador, items y configuración",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentRequestDto.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Débito Mastercard",
                                            summary = "Pago con tarjeta de débito Mastercard",
                                            value = """
                {
                  "totalAmount": 250.00,
                  "description": "Pago de consulta médica",
                  "title": "Consulta Médica General",
                  "payerName": "María González",
                  "paymentMethodId": "debmaster",
                  "paymentTypeId": "debit_card",
                  "splitPayment": false,
                  "payer": {
                    "email": "maria.gonzalez@ejemplo.com",
                    "identification": {
                      "type": "DNI",
                      "number": "12345678"
                    }
                  },
                  "token": "ff8080814c11e237014c1ff593b57b4d",
                  "installments": 1,
                  "issuerId": "24",
                  "companyId": 123
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "Débito Visa",
                                            summary = "Pago con tarjeta de débito Visa",
                                            value = """
                {
                  "totalAmount": 180.50,
                  "description": "Compra de medicamentos",
                  "title": "Medicamentos Recetados",
                  "payerName": "Carlos López",
                  "paymentMethodId": "debvisa",
                  "paymentTypeId": "debit_card",
                  "splitPayment": false,
                  "payer": {
                    "email": "carlos.lopez@correo.com",
                    "identification": {
                      "type": "DNI",
                      "number": "87654321"
                    }
                  },
                  "token": "aa1234567890abcdef1234567890abcd",
                  "installments": 1,
                  "issuerId": "11",
                  "companyId": 456
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "Crédito Mastercard",
                                            summary = "Pago con tarjeta de crédito Mastercard",
                                            value = """
                {
                  "totalAmount": 1200.00,
                  "description": "Seguro médico premium anual",
                  "title": "Seguro Sancor Plan Gold",
                  "payerName": "Ana Martínez",
                  "paymentMethodId": "master",
                  "paymentTypeId": "credit_card",
                  "splitPayment": false,
                  "payer": {
                    "email": "ana.martinez@email.com",
                    "identification": {
                      "type": "DNI",
                      "number": "11223344"
                    }
                  },
                  "token": "bb5678901234efgh5678901234efgh56",
                  "installments": 12,
                  "issuerId": "24",
                  "companyId": 789
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "Crédito Visa",
                                            summary = "Pago con tarjeta de crédito Visa",
                                            value = """
                {
                  "totalAmount": 850.75,
                  "description": "Plan de salud familiar",
                  "title": "Plan de Salud Integral",
                  "payerName": "Roberto Silva",
                  "paymentMethodId": "visa",
                  "paymentTypeId": "credit_card",
                  "splitPayment": false,
                  "payer": {
                    "email": "roberto.silva@mail.com",
                    "identification": {
                      "type": "DNI",
                      "number": "55667788"
                    }
                  },
                  "token": "cc9012345678ijkl9012345678ijkl90",
                  "installments": 6,
                  "issuerId": "11",
                  "companyId": 321
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "Crédito American Express",
                                            summary = "Pago con tarjeta de crédito American Express",
                                            value = """
                {
                  "totalAmount": 2500.00,
                  "description": "Seguro de vida premium",
                  "title": "Seguro de Vida MetLife Premium",
                  "payerName": "Patricia Rodríguez",
                  "paymentMethodId": "amex",
                  "paymentTypeId": "credit_card",
                  "splitPayment": false,
                  "payer": {
                    "email": "patricia.rodriguez@empresa.com",
                    "identification": {
                      "type": "DNI",
                      "number": "99887766"
                    }
                  },
                  "token": "dd3456789012mnop3456789012mnop34",
                  "installments": 3,
                  "issuerId": "310",
                  "companyId": 654
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "Dinero en cuenta",
                                            summary = "Pago usando saldo de cuenta MercadoPago",
                                            value = """
                {
                  "totalAmount": 150.00,
                  "description": "Recarga de crédito médico",
                  "title": "Crédito para Consultas",
                  "payerName": "Luis Fernández",
                  "paymentMethodId": "account_money",
                  "paymentTypeId": "account_money",
                  "splitPayment": false,
                  "payer": {
                    "email": "luis.fernandez@ejemplo.com",
                    "identification": {
                      "type": "DNI",
                      "number": "44556677"
                    }
                  },
                  "installments": 1,
                  "currencyId": "ARS"
                }
                """
                                    ),
                                    @ExampleObject(
                                            name = "Split Payment",
                                            summary = "Pago dividido entre múltiples empresas",
                                            value = """
                {
                  "totalAmount": 500.00,
                  "description": "Consulta médica con laboratorio",
                  "title": "Consulta + Análisis Clínicos",
                  "payerName": "Elena Castro",
                  "paymentMethodId": "visa",
                  "paymentTypeId": "credit_card",
                  "splitPayment": true,
                  "companyId": 999,
                  "payer": {
                    "email": "elena.castro@correo.com",
                    "identification": {
                      "type": "DNI",
                      "number": "33445566"
                    }
                  },
                  "token": "ee7890123456qrst7890123456qrst78",
                  "installments": 1,
                  "issuerId": "11"
                }
                """
                                    )
                            }
                    )
            )
            @RequestBody PaymentRequestDto paymentRequestDto
    ) throws Exception {
        try {
            PaymentResponseDto paymentResponseDto = paymentService.generatePayment(paymentRequestDto);

            return ResponseEntity.status(200).body(paymentResponseDto);
        } catch (MPApiException e) {
            throw new Exception("Error en Mercado Pago: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * Webhook
     * @param body body mandado en la peticion
     * @return
     * @throws Exception
     */
    @Operation(
            summary = "Webhook de notificaciones de pago",
            description = "Endpoint que recibe las notificaciones de MercadoPago cuando cambia el estado de un pago. " +
                    "Procesa la notificación y actualiza el estado del pago en la base de datos. " +
                    "El webhook incluye información completa del evento como ID, fecha de creación, acción realizada, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Webhook procesado correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"message\": \"Webhook procesado correctamente\", \"paymentId\": 123456789, \"status\": \"approved\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos del webhook inválidos o tipo no soportado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Type no válido")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Pago no encontrado en la base de datos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al procesar el webhook",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Error interno del servidor")
                    )
            )
    })
    @PostMapping("/webhook")
    public ResponseEntity<?> webhook(
            @Parameter(
                    description = "Cuerpo completo de la notificación enviada por MercadoPago. Incluye metadatos del evento, " +
                            "tipo de notificación, fecha de creación y datos específicos del pago",
                    required = true,
                    schema = @Schema(
                            type = "object",
                            example = "{\n" +
                                    "  \"id\": 12345,\n" +
                                    "  \"live_mode\": true,\n" +
                                    "  \"type\": \"payment\",\n" +
                                    "  \"date_created\": \"2015-03-25T10:04:58.396-04:00\",\n" +
                                    "  \"user_id\": 44444,\n" +
                                    "  \"api_version\": \"v1\",\n" +
                                    "  \"action\": \"payment.created\",\n" +
                                    "  \"data\": {\n" +
                                    "    \"id\": \"999999999\"\n" +
                                    "  }\n" +
                                    "}"
                    )
            )
            @RequestBody Map<String, Object> body
    ) throws Exception {
        try {
            log.info("Webhook recibido: {}", body);

            // Validar estructura del webhook
            String type = (String) body.get("type");
            if (!"payment".equals(type)) {
                log.warn("Tipo de webhook no válido: {}", type);
                return ResponseEntity.badRequest().body("Type no válido");
            }

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null || data.get("id") == null) {
                log.error("Datos del webhook incompletos");
                return ResponseEntity.badRequest().body("ID de pago ausente");
            }

            Long paymentId = Long.parseLong(data.get("id").toString());
            log.info("Procesando webhook para payment ID: {}", paymentId);

            // Buscar el pago en la base de datos
            PaymentEntity paymentEntity = paymentService.findById(paymentId);
            if (paymentEntity == null) {
                log.error("Pago no encontrado en base de datos: {}", paymentId);
                return ResponseEntity.notFound().build();
            }

            // Actualizar el estado del pago consultando MercadoPago
            PaymentEntity updatedPayment = paymentService.updatePaymentStatusFromMP(paymentId);

            log.info("Pago actualizado: PaymentID={}, DatabaseID={}, Status={}, StatusDetail={}",
                    paymentId, updatedPayment.getId(), updatedPayment.getStatus(), updatedPayment.getStatusDetail());

            return ResponseEntity.ok(Map.of(
                    "message", "Webhook procesado correctamente",
                    "paymentId", paymentId,
                    "status", updatedPayment.getStatus()
            ));

        } catch (NumberFormatException e) {
            log.error("Error al parsear payment ID", e);
            return ResponseEntity.badRequest().body("ID de pago inválido");
        } catch (Exception e) {
            log.error("Error procesando webhook", e);
            return ResponseEntity.status(500).body("Error interno del servidor");
        }
    }
}
