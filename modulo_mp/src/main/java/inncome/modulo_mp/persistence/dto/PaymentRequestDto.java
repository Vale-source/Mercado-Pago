package inncome.modulo_mp.persistence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Optional;

@Data
@Builder
@Schema(description = "DTO para crear una nueva preferencia de pago en MercadoPago")
public class PaymentRequestDto {

    @Schema(
            description = "Monto total del pago",
            example = "100.50",
            required = true
    )
    private Float totalAmount;

    @Schema(
            description = "ID de la empresa que recibe el pago (opcional para split payment)",
            example = "123",
            required = false
    )
    private Optional<Long> companyId;

    @NotBlank
    @Schema(
            description = "Descripción detallada del pago o producto",
            example = "Pago de seguro médico plan premium",
            required = true
    )
    private String description;

    @Schema(
            description = "Indica si es un pago dividido (split payment)",
            example = "true",
            required = false,
            defaultValue = "false"
    )
    private Boolean splitPayment;

    @NotBlank
    @Schema(
            description = "Tipo de pago utilizado para la estrategia de procesamiento",
            example = "account_money",
            required = true,
            allowableValues = {"account_money", "debit_card", "credit_card"}
    )
    private String paymentTypeId;

    @NotBlank
    @Schema(
            description = "Título del item a comprar",
            example = "Seguro Sancor Plan Gold",
            required = true
    )
    private String title;

    @NotBlank
    @Schema(
            description = "Nombre completo del pagador",
            example = "Juan Pérez",
            required = true
    )
    private String payerName;

    @NotNull
    @Schema(
            description = "Método de pago específico de MercadoPago",
            example = "account_money",
            required = true,
            allowableValues = {"account_money", "debmaster", "debvisa", "amex", "visa", "master"}
    )
    private String paymentMethodId;

    @Schema(
            description = "Datos completos del pagador",
            required = true,
            implementation = PayerDto.class
    )
    private PayerDto payer;

    @Schema(
            description = "Moneda del pago. Solo se usa cuando se paga con dinero en cuenta de MercadoPago",
            example = "ARS",
            required = false,
            allowableValues = {"ARS", "USD", "BRL"}
    )
    private Optional<String> currencyId;

    @Schema(
            description = "Token de la tarjeta para pagos con débito/crédito. Obligatorio para pagos con tarjeta",
            example = "ff8080814c11e237014c1ff593b57b4d",
            required = false
    )
    private Optional<String> token;

    @Schema(
            description = "Número de cuotas para pagos con tarjeta de crédito",
            example = "12",
            required = false,
            minimum = "1",
            maximum = "24"
    )
    private Optional<Integer> installments;

    @Schema(
            description = "ID del banco emisor de la tarjeta. Obligatorio para pagos con tarjeta",
            example = "24",
            required = false
    )
    private Optional<String> issuerId;
}
