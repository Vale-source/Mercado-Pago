package inncome.modulo_mp.service;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public interface OAuthService {
    URI generateAuth (Optional<Long> companyTokenId) throws NoSuchAlgorithmException, MPException, MPApiException;
}
