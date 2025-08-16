package inncome.modulo_mp.persistence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Respuesta con los datos del pago procesado por MercadoPago")
public class PaymentResponseDto {

    @Schema(
            description = "ID único del pago generado por MercadoPago",
            example = "123456789"
    )
    private String id; //ID del pago (el que genera mercado pago)

    @Schema(
            description = "Descripción detallada de la compra realizada",
            example = "Pago de seguro médico plan premium"
    )
    private String description; // Descripcion de la compra

    @Schema(
            description = "Estado actual del pago en MercadoPago",
            example = "approved",
            allowableValues = {"pending", "approved", "authorized", "in_process", "in_mediation", "rejected", "cancelled", "refunded", "charged_back"}
    )
    private String status; //Estado del pago

    @Schema(
            description = "Detalles específicos del estado del pago",
            example = "accredited",
            allowableValues = {"accredited", "pending_contingency", "pending_review_manual", "cc_rejected_other_reason", "cc_rejected_call_for_authorize", "cc_rejected_insufficient_amount"}
    )
    private String statusDetail; //Detalles del pago

    @Schema(
            description = "Método de pago utilizado (marca de tarjeta o cuenta)",
            example = "visa",
            allowableValues = {"account_money", "debmaster", "debvisa", "amex", "visa", "master"}
    )
    private String paymentMethodId; //Metodo de pago (visa, master, etc)

    @Schema(
            description = "Tipo de pago realizado",
            example = "credit_card",
            allowableValues = {"account_money", "debit_card", "credit_card"}
    )
    private String paymentTypeId; //Tipo de pago (credito, debito, etc)

    @Schema(
            description = "Monto total procesado del pago",
            example = "100.50",
            minimum = "0.01"
    )
    private Float transactionAmount; //Total a pagar
}
