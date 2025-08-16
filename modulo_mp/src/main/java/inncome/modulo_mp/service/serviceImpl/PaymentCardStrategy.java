package inncome.modulo_mp.service.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import inncome.modulo_mp.config.MPConfig;
import inncome.modulo_mp.persistence.dto.PaymentRequestDto;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.persistence.entity.PaymentEntity;
import inncome.modulo_mp.strategy.PaymentGenerationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static inncome.modulo_mp.utils.PaymentUtils.*;

@Slf4j
@Component
public class PaymentCardStrategy implements PaymentGenerationStrategy {

    private final MPConfig mpConfig;

    public PaymentCardStrategy(MPConfig mpConfig) {
        this.mpConfig = mpConfig;
    }

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Genera una entidad de pago utilizando tarjetas de crédito o débito con MercadoPago.
     *
     * <p>Este metodo procesa solicitudes de pago con tarjetas, validando los parámetros
     * requeridos (token, cuotas, banco) y creando tanto preferencias como órdenes según
     * el tipo de pago. Soporta pagos simples mediante API Orders y pagos divididos
     * (split payment) mediante preferencias.</p>
     *
     * <p>Flujo de procesamiento:</p>
     * <ul>
     *   <li>Validación de token, cuotas e ID del banco</li>
     *   <li>Configuración del token de acceso según el tipo de pago</li>
     *   <li>Para split payment: creación de preferencia con comisión del 10%</li>
     *   <li>Para pagos regulares: creación de orden mediante API Orders</li>
     * </ul>
     *
     * <p>Para pagos divididos (splitPayment = true):</p>
     * <ul>
     *   <li>Utiliza el token de acceso de la empresa</li>
     *   <li>Aplica comisión del 10% sobre el monto total</li>
     *   <li>Crea una preferencia de MercadoPago</li>
     *   <li>Calcula el monto neto después de la comisión</li>
     * </ul>
     *
     * <p>Para pagos regulares:</p>
     * <ul>
     *   <li>Utiliza el token de acceso global configurado</li>
     *   <li>Crea una orden mediante API Orders de MercadoPago</li>
     *   <li>Incluye información del metodo de pago, cuotas y token</li>
     *   <li>Genera referencia externa única para la transacción</li>
     * </ul>
     *
     * @param dto el objeto de solicitud de pago que debe incluir token de tarjeta,
     *            número de cuotas, ID del banco emisor y toda la información del pagador
     * @param companyToken el token de la empresa utilizado para pagos divididos. Puede ser null
     *                     para pagos regulares que utilizan el token global
     *
     * @return PaymentEntity la entidad de pago creada con información completa incluyendo
     *         ID de pago/orden, estado, metodo de pago, montos y datos del pagador
     *
     * @throws MPException si el token, cuotas o ID del banco están vacíos
     * @throws Exception si ocurre un error durante la comunicación con la API de MercadoPago
     *                   o al procesar la respuesta JSON
     *
     * @see PaymentRequestDto
     * @see CompanyToken
     * @see PaymentEntity
     * @see Preference
     * @since 1.0
     */
    @Override
    public PaymentEntity generatePayment(PaymentRequestDto dto, CompanyToken companyToken) throws Exception {
        if (dto.getToken().isEmpty()){
            throw new MPException("Token vacio en el dto");
        } else if (dto.getInstallments().isEmpty()) {
            throw new MPException("Cuotas vacio en el dto");
        } else if (dto.getIssuerId().isEmpty()) {
            throw new MPException("Banco vacio en el dto");
        }

        if (dto.getSplitPayment() && companyToken != null) {
            MercadoPagoConfig.setAccessToken(companyToken.getAccessToken());

            PreferenceClient preferenceClient = new PreferenceClient ();

            double percentage = 0.1;
            double commision = dto.getTotalAmount() * percentage;
            double netAmount = dto.getTotalAmount() - commision;

            //Genero la identificacion (DNI y numero)
            IdentificationRequest identificationRequest = IdentificationRequest.builder()
                    .type(dto.getPayer().getIdentification().getType())
                    .number(dto.getPayer().getIdentification().getNumber())
                    .build();

            //Genero el comprador
            PreferencePayerRequest preferencePayerRequest = PreferencePayerRequest.builder()
                    .email(dto.getPayer().getEmail())
                    .identification(identificationRequest)
                    .build();

            List<PreferenceItemRequest> items = createItemRequest(dto.getTitle(), dto.getDescription(), dto.getCurrencyId().get(), BigDecimal.valueOf(dto.getTotalAmount()));

            Preference preference = createPreference(items, preferencePayerRequest, commision, preferenceClient);

            return getPaymentEntity(dto, commision, netAmount, items, preference);
        } else {
            MercadoPagoConfig.setAccessToken(mpConfig.getAccessToken());

            //API ORDER

            String idempotencyKey = UUID.randomUUID().toString();
            String token = String.format("Bearer %s", mpConfig.getAccessToken());

            Map<String, Object> body = new HashMap<>();
            body.put("type", "online");
            body.put("external_reference", generateExternalReference());
            body.put("capture_mode", "automatic");
            body.put("processing_mode", "automatic");
            body.put("total_amount", dto.getTotalAmount().toString());
            body.put("payer", Map.of(
                    "first_name", dto.getPayerName(),
                    "email", dto.getPayer().getEmail(),
                    "identification", Map.of(
                            "type", dto.getPayer().getIdentification().getType(),
                            "number", dto.getPayer().getIdentification().getNumber()
                    )
            ));
            body.put("transactions", Map.of(
                    "payments", new Object[] {
                        Map.of(
                            "amount", dto.getTotalAmount().toString(),
                            "payment_method", Map.of(
                                "id", dto.getPaymentMethodId(),
                                "type", dto.getPaymentTypeId(),
                                "token", dto.getToken().get(),
                                "installments", dto.getInstallments().orElse(1)
                            )
                        )
                    }
            ));
            body.put("items", new Object[] {
                Map.of(
                    "title", dto.getTitle(),
                    "unit_price", dto.getTotalAmount().toString(),
                    "quantity", 1,
                    "description", dto.getDescription()
                )
            });

            String order = webClientBuilder.build()
                    .post()
                    .uri("https://api.mercadopago.com/v1/orders")
                    .header("Content-Type","application/json")
                    .header("X-Idempotency-Key", idempotencyKey)
                    .header("Authorization", token)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode orderJson = objectMapper.readTree(order);
            JsonNode payment = orderJson.get("transactions").get("payments").get(0);
            JsonNode paymentMethod = payment.get("payment_method");

            return PaymentEntity.builder()
                    .payment_id(orderJson.get("id").asText()) // Si quiero persistir el id solamente del pago payment.get("id").asText()
                    .status(payment.get("status").asText())
                    .description(dto.getDescription())
                    .statusDetail(payment.get("status_detail").asText())
                    .paymentType(paymentMethod.get("type").asText())
                    .paymentMethod(paymentMethod.get("id").asText())
                    .payerType(dto.getPayer().getIdentification().getType())
                    .payerEmail(dto.getPayer().getEmail())
                    .payerNumber(dto.getPayer().getIdentification().getNumber())
                    .totalAmount(orderJson.get("total_paid_amount").asDouble())
                    .marketplaceFee(0)
                    .netReceivedAmount(orderJson.get("total_paid_amount").asDouble())
                    .currencyId(String.valueOf(dto.getCurrencyId()))
                    .dateCreated(orderJson.get("created_date").asText())
                    .dateApproved(orderJson.get("last_updated_date").asText())
                    .build();
        }

    }

    public static String  generateExternalReference() {
        SecureRandom random = new SecureRandom();
        String aviableCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

        StringBuilder externalReference = new StringBuilder();

        for (int i = 0; i < 64; i++) {
            int index = random.nextInt(aviableCharacters.length());
            externalReference.append(aviableCharacters.charAt(index));
        }

        return externalReference.toString();
    }
}
