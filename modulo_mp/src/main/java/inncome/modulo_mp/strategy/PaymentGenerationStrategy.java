package inncome.modulo_mp.strategy;


import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import inncome.modulo_mp.persistence.dto.PaymentRequestDto;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.persistence.entity.PaymentEntity;

public interface PaymentGenerationStrategy {
    PaymentEntity generatePayment(PaymentRequestDto dto, CompanyToken companyToken) throws Exception;
}
