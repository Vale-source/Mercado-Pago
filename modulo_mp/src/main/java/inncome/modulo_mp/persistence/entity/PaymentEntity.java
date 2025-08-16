package inncome.modulo_mp.persistence.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("payment_id")
    @Column(name = "payment_id")
    private String payment_id; // ID del pago que genera MercadoPago

    @NotBlank
    @JsonProperty("status")
    private String status; // Estado del pago

    @NotBlank
    @JsonProperty("description")
    private String description;

    @NotBlank
    @JsonProperty("statusDetail")
    @Column(name ="status_detail")
    private String statusDetail; // Detalles del pago, por si fue rechazado

    @NotBlank
    @JsonProperty("paymentType")
    @Column(name = "payment_type_id")
    private String paymentType; // Tipo de pago, tarjeta de credito, etc

    @NotBlank
    @JsonProperty("paymentMethod")
    @Column(name = "payment_method_id")
    private String paymentMethod; // Metodo de pago, visa, mastercard, etc

    @NotBlank
    @JsonProperty("payerType")
    @Column(name = "payer_identification_type")
    private String payerType; // Tipo de documento de quien realiza el pago

    @NotBlank
    @JsonProperty("payerEmail")
    private String payerEmail; // Correo del comprador

    @NotBlank
    @JsonProperty("payerNumber")
    @Column(name = "payer_identification_number")
    private String payerNumber; // Numero de documento

    @JsonProperty("totalAmount")
    @Column(name = "transaction_amount")
    private double totalAmount; // Total que se pago

    @Column(name = "commission")
    @JsonProperty("commission")
    private double marketplaceFee; // Comision que se lleva la empresa

    @JsonProperty("sellerTotal")
    @Column(name = "net_received_amount")
    private double netReceivedAmount; // Lo que se lleva el vendedor

    @NotBlank
    @JsonProperty("currency")
    @Column(name = "currency_id")
    private String currencyId; // Divisa

    @NotBlank
    @JsonProperty("dateCreated")
    @Column(name = "date_created")
    private String dateCreated; // Fecha que se creo el pago

    @NotBlank
    @JsonProperty("dateApproved")
    @Column(name = "date_approved")
    private String dateApproved; // Fecha que se aprobo el pago
}
