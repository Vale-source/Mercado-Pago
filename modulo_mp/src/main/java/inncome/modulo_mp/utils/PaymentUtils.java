package inncome.modulo_mp.utils;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import inncome.modulo_mp.persistence.dto.PaymentRequestDto;
import inncome.modulo_mp.persistence.entity.PaymentEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PaymentUtils {

    /**
     * Construye una entidad PaymentEntity a partir de los datos de preferencia y comisiones.
     *
     * <p>Este metodo crea una instancia completa de PaymentEntity utilizando información
     * de la preferencia de MercadoPago, datos de la solicitud original y cálculos de
     * comisiones. Es utilizado principalmente después de crear una preferencia exitosa
     * para persistir los datos del pago en la base de datos local.</p>
     *
     * <p>La entidad creada incluye:</p>
     * <ul>
     *   <li>ID de la preferencia como payment_id</li>
     *   <li>Estado inicial "pending"</li>
     *   <li>Información completa del pagador</li>
     *   <li>Montos totales, comisiones y netos calculados</li>
     *   <li>Metadatos de fecha y tipo de operación</li>
     * </ul>
     *
     * @param dto objeto de solicitud de pago que contiene la descripción y datos originales
     * @param commision monto de la comisión aplicada al pago (generalmente 10% para split payments)
     * @param netAmount monto neto que recibe el comercio después de descontar comisiones
     * @param items lista de elementos de la preferencia que contiene información del producto/servicio
     * @param preference preferencia de MercadoPago creada exitosamente con todos los datos del pago
     *
     * @return PaymentEntity entidad completa lista para ser persistida en la base de datos
     *
     * @see PaymentEntity
     * @see PaymentRequestDto
     * @see Preference
     * @see PreferenceItemRequest
     * @since 1.0
     */
    public static PaymentEntity getPaymentEntity(PaymentRequestDto dto, double commision, double netAmount, List<PreferenceItemRequest> items, Preference preference) {
        return PaymentEntity.builder()
                .payment_id(preference.getId())
                .status("pending")
                .description(dto.getDescription())
                .statusDetail("pending")
                .paymentType(preference.getOperationType())
                .paymentMethod(dto.getPaymentMethodId())
                .payerType(preference.getPayer().getIdentification().getType())
                .payerEmail(preference.getPayer().getEmail())
                .payerNumber(preference.getPayer().getIdentification().getNumber())
                .totalAmount(items.getFirst().getUnitPrice().doubleValue())
                .marketplaceFee(commision)
                .netReceivedAmount(netAmount)
                .currencyId(preference.getItems().getFirst().getCurrencyId())
                .dateCreated(preference.getDateCreated().toString())
                .dateApproved(preference.getDateCreated().toString())
                .build();
    }

    /**
     * Crea una lista de elementos de preferencia para MercadoPago.
     *
     * <p>Este metodo construye los elementos que representan los productos o servicios
     * a pagar en una preferencia de MercadoPago. Cada elemento incluye información
     * detallada como título, descripción, precio y moneda.</p>
     *
     * <p>Características del elemento creado:</p>
     * <ul>
     *   <li>Cantidad fija de 1 unidad</li>
     *   <li>ID y título idénticos para identificación</li>
     *   <li>Soporte para múltiples monedas</li>
     *   <li>Descripción detallada del producto/servicio</li>
     * </ul>
     *
     * @param title título y identificador único del producto o servicio
     * @param description descripción detallada del producto o servicio a pagar
     * @param currency código de moneda (ej: "ARS", "USD", "BRL")
     * @param unitPrice precio unitario del producto expresado como BigDecimal
     *
     * @return List<PreferenceItemRequest> lista que contiene un elemento de preferencia configurado
     *
     * @see PreferenceItemRequest
     * @since 1.0
     */
    public static List<PreferenceItemRequest> createItemRequest(
            String title,
            String description,
            String currency,
            BigDecimal unitPrice
    ) {

        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id(title)
                .title(title)
                .description(description)
                .quantity(1)
                .currencyId(currency)
                .unitPrice(unitPrice)
                .build();

        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(itemRequest);

     return items;
    }

    /**
     * Crea una preferencia completa de MercadoPago con configuración de URLs y comisiones.
     *
     * <p>Este metodo construye una preferencia completa de MercadoPago incluyendo
     * elementos de pago, información del pagador, URLs de retorno, webhooks y
     * configuración de marketplace con comisiones. Es el metodo principal para
     * generar preferencias listas para procesar pagos.</p>
     *
     * <p>Configuración incluida:</p>
     * <ul>
     *   <li>URLs de retorno para éxito, pendiente y falla</li>
     *   <li>URL de webhook para notificaciones automáticas</li>
     *   <li>Configuración de marketplace "Inncome"</li>
     *   <li>Comisión del marketplace en metadatos y monto</li>
     *   <li>Auto-retorno cuando el pago es aprobado</li>
     * </ul>
     *
     * <p>URLs de retorno configuradas:</p>
     * <ul>
     *   <li>Éxito: https://http.cat/200</li>
     *   <li>Pendiente: https://http.cat/102</li>
     *   <li>Falla: https://http.cat/500</li>
     * </ul>
     *
     * @param items lista de elementos de la preferencia que representan productos/servicios
     * @param payer información completa del pagador incluyendo identificación y email
     * @param comission monto de comisión que se aplicará al pago (stored in metadata and marketplace fee)
     * @param client cliente de MercadoPago configurado para crear la preferencia
     *
     * @return Preference preferencia creada exitosamente en MercadoPago lista para procesar pagos
     *
     * @throws MPException si ocurre un error en la configuración de MercadoPago
     * @throws MPApiException si hay un error en la comunicación con la API de MercadoPago
     *
     * @see PreferenceItemRequest
     * @see PreferencePayerRequest
     * @see PreferenceClient
     * @see Preference
     * @since 1.0
     */
    public static Preference createPreference(
            List<PreferenceItemRequest> items,
            PreferencePayerRequest payer,
            double comission,
            PreferenceClient client
    ) throws MPException, MPApiException {
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
                        "Inncome", comission
                ))
                .notificationUrl("https://api.app.inncome.net/api/mp/payment/webhook")
                .marketplaceFee(BigDecimal.valueOf(comission))
                .backUrls(backUrlsRequest)
                .autoReturn("approved")
                .build();

        return client.create(preferenceRequest);
    }
}
