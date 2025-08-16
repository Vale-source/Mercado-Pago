package inncome.modulo_mp.service;

import inncome.modulo_mp.persistence.dto.PaymentRequestDto;
import inncome.modulo_mp.persistence.dto.PaymentResponseDto;
import inncome.modulo_mp.persistence.entity.PaymentEntity;

public interface PaymentService {
    PaymentResponseDto generatePayment (PaymentRequestDto dto) throws Exception;
    PaymentEntity findById (Long payment_id) throws Exception;
}
