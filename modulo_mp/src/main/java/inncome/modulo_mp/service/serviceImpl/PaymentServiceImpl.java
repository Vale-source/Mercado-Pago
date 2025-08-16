package inncome.modulo_mp.service.serviceImpl;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import inncome.modulo_mp.config.MPConfig;
import inncome.modulo_mp.exception.ResourceNotFoundException;
import inncome.modulo_mp.factory.MPStrategyFactory;
import inncome.modulo_mp.persistence.dto.PaymentRequestDto;
import inncome.modulo_mp.persistence.dto.PaymentResponseDto;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.persistence.entity.PaymentEntity;
import inncome.modulo_mp.persistence.repository.CompanyTokenRepository;
import inncome.modulo_mp.persistence.repository.PaymentRepository;
import inncome.modulo_mp.service.PaymentService;
import inncome.modulo_mp.strategy.PaymentGenerationStrategy;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private CompanyTokenRepository companyTokenRepository;

    @Autowired
    private MercadoPagoAuthServiceImpl authService;

    @Autowired
    private MPStrategyFactory strategyFactory;

    @Autowired
    private MPConfig mpConfig;

    /**
     * Genera un pago utilizando MercadoPago según el tipo de pago especificado.
     *
     * <p>Este metodo procesa solicitudes de pago utilizando el patrón Strategy para
     * seleccionar la implementación apropiada según el tipo de pago. Soporta tanto
     * pagos regulares como pagos divididos (split payment) con manejo automático
     * de tokens de autenticación y comisiones.</p>
     *
     * <p>Flujo de procesamiento:</p>
     * <ul>
     *   <li>Selección de estrategia según el tipo de pago</li>
     *   <li>Para split payment: validación y refresh del token de empresa</li>
     *   <li>Generación del pago mediante la estrategia seleccionada</li>
     *   <li>Construcción de la respuesta con datos del pago</li>
     *   <li>Persistencia del pago en la base de datos</li>
     * </ul>
     *
     * <p>Para pagos divididos (splitPayment = true):</p>
     * <ul>
     *   <li>Busca y valida la empresa por ID</li>
     *   <li>Refresca el token de autenticación automáticamente</li>
     *   <li>Aplica comisiones según la configuración</li>
     * </ul>
     *
     * @param dto el objeto de solicitud de pago que contiene todos los datos necesarios
     *            incluyendo monto, tipo de pago, datos del pagador y configuración
     *
     * @return PaymentResponseDto objeto de respuesta con información del pago generado
     *         incluyendo ID, estado, metodo de pago y monto
     *
     * @throws ResourceNotFoundException si no se encuentra la empresa para pagos divididos
     * @throws Exception si ocurre un error durante la generación del pago o comunicación con MercadoPago
     *
     * @see PaymentRequestDto
     * @see PaymentResponseDto
     * @see PaymentGenerationStrategy
     * @since 1.0
     */
    @Transactional
    @Override
    public PaymentResponseDto generatePayment (PaymentRequestDto dto) throws Exception {

        //Genero la estrategia
        PaymentGenerationStrategy strategy = strategyFactory.getStrategy(dto.getPaymentTypeId());

        if (dto.getSplitPayment()) {
            CompanyToken company = companyTokenRepository.findCompanyTokenById(dto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("No se encontro la compañia"));
            authService.refreshToken(company);

            PaymentEntity payment = strategy.generatePayment(dto, company);

            PaymentResponseDto responseDto =  PaymentResponseDto.builder()
                    .id(payment.getPayment_id())
                    .description(payment.getDescription())
                    .status(payment.getStatus())
                    .statusDetail(payment.getStatusDetail())
                    .paymentMethodId(payment.getPaymentMethod())
                    .paymentTypeId(payment.getPaymentType())
                    .transactionAmount((float) payment.getTotalAmount())
                    .build();


            paymentRepository.save(payment);

            return responseDto;
        } else {
            PaymentEntity payment = strategy.generatePayment(dto, null);

            PaymentResponseDto responseDto =  PaymentResponseDto.builder()
                    .id(payment.getPayment_id())
                    .description(payment.getDescription())
                    .status(payment.getStatus())
                    .statusDetail(payment.getStatusDetail())
                    .paymentMethodId(payment.getPaymentMethod())
                    .paymentTypeId(payment.getPaymentType())
                    .transactionAmount((float) payment.getTotalAmount())
                    .build();


            paymentRepository.save(payment);

            return responseDto;
        }

    }

    /**
     * Busca un pago por su ID en la base de datos local.
     *
     * <p>Este metodo es utilizado principalmente por el sistema de webhooks para
     * localizar pagos existentes cuando se reciben notificaciones de cambio de estado
     * desde MercadoPago. Realiza una búsqueda directa en la base de datos local.</p>
     *
     * @param payment_id el ID único del pago a buscar en la base de datos
     *
     * @return PaymentEntity la entidad del pago encontrada, o null si no existe
     *
     * @see PaymentEntity
     * @since 1.0
     */
    @Override
    public PaymentEntity findById (Long payment_id)  {
        return paymentRepository.findById(payment_id).orElse(null);
    }


    /**
     * Actualiza el estado de un pago consultando directamente la API de MercadoPago.
     *
     * <p>Este metodo es utilizado principalmente por webhooks para sincronizar el estado
     * de pagos locales con el estado real en MercadoPago. Es especialmente útil para
     * pagos generados con Checkout Pro donde el estado inicial es "pendiente" y debe
     * actualizarse una vez que el usuario completa el pago.</p>
     *
     * <p>Proceso de actualización:</p>
     * <ul>
     *   <li>Configura el token de acceso global de MercadoPago</li>
     *   <li>Consulta el estado actual del pago en la API de MercadoPago</li>
     *   <li>Busca el pago correspondiente en la base de datos local</li>
     *   <li>Actualiza los campos status y statusDetail</li>
     *   <li>Persiste los cambios en la base de datos</li>
     * </ul>
     *
     * @param paymentId el ID del pago en MercadoPago que se desea actualizar
     *
     * @return PaymentEntity la entidad del pago actualizada con el nuevo estado
     *
     * @throws MPApiException si ocurre un error al consultar la API de MercadoPago
     * @throws Exception si no se puede consultar el estado del pago o actualizar la base de datos
     *
     * @see PaymentEntity
     * @see PaymentClient
     * @since 1.0
     */

    public PaymentEntity updatePaymentStatusFromMP(Long paymentId) throws Exception {
        try {
            // Configurar MercadoPago (usar el token apropiado)
            MercadoPagoConfig.setAccessToken(mpConfig.getAccessToken());

            // Consultar el pago en MercadoPago
            PaymentClient paymentClient = new PaymentClient();
            Payment mpPayment = paymentClient.get(paymentId);

            // Buscar el pago en la base de datos
            PaymentEntity paymentEntity = findById(paymentId);

            // Actualizar status y status_detail
            paymentEntity.setStatus(mpPayment.getStatus());
            paymentEntity.setStatusDetail(mpPayment.getStatusDetail());

            // Guardar cambios
            return paymentRepository.save(paymentEntity);

        } catch (MPApiException e) {
            log.error("Error consultando pago en MercadoPago: {}", e.getApiResponse().getContent());
            throw new Exception("Error consultando estado del pago en MercadoPago");
        }
    }
}
