package inncome.modulo_mp.service.serviceImpl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.preference.*;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentPayer;
import com.mercadopago.resources.preference.Preference;
import inncome.modulo_mp.config.MPConfig;
import inncome.modulo_mp.persistence.dto.PaymentRequestDto;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.persistence.entity.Enum.PaymentType;
import inncome.modulo_mp.persistence.entity.PaymentEntity;
import inncome.modulo_mp.strategy.PaymentGenerationStrategy;
import inncome.modulo_mp.utils.PaymentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static inncome.modulo_mp.utils.PaymentUtils.*;

@Component
public class PaymentAccountMoneyStrategy implements PaymentGenerationStrategy {

    private final MPConfig mpConfig;

    public PaymentAccountMoneyStrategy(MPConfig mpConfig) {
        this.mpConfig = mpConfig;
    }

    /**
     * Genera una entidad de pago utilizando el metodo "account_money" de MercadoPago.
     *
     * <p>Este metodo procesa solicitudes de pago con dinero en cuenta de MercadoPago,
     * validando los parámetros requeridos y creando las preferencias correspondientes.
     * Soporta tanto pagos simples como pagos divididos (split payment) con aplicación
     * automática de comisiones.</p>
     *
     * <p>Flujo de procesamiento:</p>
     * <ul>
     *   <li>Validación del metodo de pago (debe ser "account_money")</li>
     *   <li>Validación de la moneda requerida</li>
     *   <li>Configuración del token de acceso según el tipo de pago</li>
     *   <li>Creación de la preferencia de MercadoPago</li>
     *   <li>Generación de la entidad de pago resultante</li>
     * </ul>
     *
     * <p>Para pagos divididos (splitPayment = true):</p>
     * <ul>
     *   <li>Aplica una comisión del 10% sobre el monto total</li>
     *   <li>Utiliza el token de acceso de la empresa proporcionada</li>
     *   <li>Calcula el monto neto después de la comisión</li>
     * </ul>
     *
     * <p>Para pagos regulares:</p>
     * <ul>
     *   <li>Utiliza el token de acceso global configurado</li>
     *   <li>No aplica comisiones adicionales</li>
     *   <li>Configura URLs de retorno estándar</li>
     * </ul>
     *
     * @param dto el objeto de solicitud de pago que contiene toda la información necesaria
     *            incluyendo monto, descripción, datos del pagador y configuración de pago
     * @param companyToken el token de la empresa utilizado para pagos divididos. Puede ser null
     *                     para pagos regulares que utilizan el token global
     *
     * @return PaymentEntity la entidad de pago creada con toda la información procesada,
     *         incluyendo ID de preferencia, montos, comisiones y metadatos
     *
     * @throws Exception si el metodo de pago no es "account_money"
     * @throws Exception si el tipo de moneda está vacío o no especificado
     * @throws Exception si ocurre un error durante la creación de la preferencia en MercadoPago
     *
     * @see PaymentRequestDto
     * @see CompanyToken
     * @see PaymentEntity
     * @see Preference
     * @since 1.0
     */
    @Override
    public PaymentEntity generatePayment(PaymentRequestDto dto, CompanyToken companyToken) throws Exception {

        if (!Objects.equals(dto.getPaymentMethodId(), PaymentType.ACCOUNT_MONEY.name().toLowerCase(Locale.ROOT)) || !Objects.equals(dto.getPaymentTypeId(), PaymentType.ACCOUNT_MONEY.name().toLowerCase(Locale.ROOT))) {
            throw new Exception("El metodo de pago debe ser account_money");
        } else if (dto.getCurrencyId().isEmpty()) {
            throw new Exception("El tipo de moneda esta vacio");
        }

        PreferenceClient client = new PreferenceClient ();

        IdentificationRequest identificationRequest = IdentificationRequest.builder()
                .type(dto.getPayer().getIdentification().getType())
                .number(dto.getPayer().getIdentification().getNumber())
                .build();

        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(dto.getPayer().getEmail())
                .identification(identificationRequest)
                .build();

        if (dto.getSplitPayment() && companyToken != null) {

            MercadoPagoConfig.setAccessToken(companyToken.getAccessToken());

            double percentage = 0.1;
            double commision = dto.getTotalAmount() * percentage;
            double netAmount = dto.getTotalAmount() - commision;

            List<PreferenceItemRequest> items = createItemRequest(dto.getTitle(), dto.getDescription(), dto.getCurrencyId().get(), BigDecimal.valueOf(dto.getTotalAmount()));

            Preference preference = createPreference(items, payer, commision, client);

            return getPaymentEntity(dto, commision, netAmount, items, preference);
        } else {
            MercadoPagoConfig.setAccessToken(mpConfig.getAccessToken());

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(dto.getTitle())
                    .title(dto.getTitle())
                    .description(dto.getDescription())
                    .quantity(1)
                    .currencyId(dto.getCurrencyId().get())
                    .unitPrice(BigDecimal.valueOf(dto.getTotalAmount()))
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            PreferenceBackUrlsRequest backUrlsRequest = PreferenceBackUrlsRequest.builder()
                    .success("https://http.cat/200") // Colocar URL de pagina de confirmacion o landing
                    .pending("https://http.cat/102") // Pagina de pendiente
                    .failure("https://http.cat/500") // Pagina de falla
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .marketplace("Inncome")
                    .payer(payer)
                    .metadata(Map.of(
                            "Inncome", dto.getTotalAmount()
                    ))
                    .notificationUrl("https://api.app.inncome.net/api/mp/payment/webhook")
                    .backUrls(backUrlsRequest)
                    .autoReturn("approved")
                    .build();

            Preference preference = client.create(preferenceRequest);

            return getPaymentEntity(dto, 0.0, dto.getTotalAmount(), items, preference);
        }
    }


}
