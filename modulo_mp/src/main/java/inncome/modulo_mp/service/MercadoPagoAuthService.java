package inncome.modulo_mp.service;

import inncome.modulo_mp.persistence.entity.CompanyToken;

import java.util.Optional;

public interface MercadoPagoAuthService {
    void changeCodeForToken(String code, Optional<Long> companyTokenId) throws Exception;
    void refreshToken(CompanyToken companyToken) throws Exception;
}
