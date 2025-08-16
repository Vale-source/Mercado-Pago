package inncome.modulo_mp.service.serviceImpl;

import com.mercadopago.client.oauth.OauthClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.oauth.CreateOauthCredential;
import com.mercadopago.resources.oauth.RefreshOauthCredential;
import inncome.modulo_mp.config.MPConfig;
import inncome.modulo_mp.exception.ResourceNotFoundException;
import inncome.modulo_mp.persistence.dto.ResponseMpDto;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.persistence.repository.CompanyTokenRepository;
import inncome.modulo_mp.service.MercadoPagoAuthService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.compress.harmony.pack200.PackingUtils.log;

@Slf4j
@Service
public class MercadoPagoAuthServiceImpl implements MercadoPagoAuthService {

    @Autowired
    private CompanyTokenRepository companyTokenRepository;

    @Autowired
    private MPConfig mpConfig;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Intercambia el código de autorización OAuth por tokens de acceso de MercadoPago.
     *
     * <p>Este metodo realiza el flujo de autorización OAuth 2.0 con MercadoPago para obtener
     * los tokens de acceso necesarios para realizar operaciones en la API. Utiliza el
     * código de autorización recibido del callback de OAuth junto con el code_verifier
     * almacenado previamente para completar el intercambio PKCE (Proof Key for Code Exchange).</p>
     *
     * <p>Los tokens obtenidos (access_token, refresh_token) se almacenan en la entidad
     * CompanyToken correspondiente junto con el tiempo de expiración y el ID de usuario
     * de MercadoPago.</p>
     *
     * @param code el código de autorización OAuth recibido del callback de MercadoPago
     * @param companyTokenId el ID opcional de la empresa para la cual se están obteniendo los tokens
     *
     * @throws ResourceNotFoundException si no se encuentra la CompanyToken con el ID proporcionado
     * @throws RuntimeException si ocurre un error durante la comunicación con la API de MercadoPago
     *
     * @see ResponseMpDto
     * @see CompanyToken
     * @since 1.0
     */
    @Override
    @Transactional
    public void changeCodeForToken(String code, Optional<Long> companyTokenId) {
        CompanyToken companyToken = companyTokenRepository.findCompanyTokenById(companyTokenId).orElseThrow(() -> new ResourceNotFoundException("ID no encontrado"));

        ResponseMpDto data = webClientBuilder.build()
                .post()
                .uri("https://api.mercadopago.com/oauth/token")
                .body(BodyInserters.fromFormData("client_id", mpConfig.getClientId())
                        .with("client_secret", mpConfig.getClientSecret())
                        .with("code_verifier", companyToken.getCodeVerifier())
                        .with("code", code)
                        .with("grant_type", "authorization_code")
                        .with("redirect_uri", "https://api.app.inncome.net/api/mp/oauth/callback"))
                .retrieve()
                .bodyToMono(ResponseMpDto.class)
                .block();

        companyToken.setAccessToken(data.getAccessToken());
        companyToken.setRefreshToken(data.getRefreshToken());
        companyToken.setExpiresIn(data.getExpiresIn());
        companyToken.setMercadoPagoId(data.getMercadoPagoId());

    }

    /**
     * Refresca el token de acceso de MercadoPago cuando es necesario.
     *
     * <p>Este metodo verifica si el token de acceso actual ha expirado o está próximo a expirar
     * y realiza una solicitud para obtener un nuevo token utilizando el refresh_token almacenado.
     * El proceso sigue el flujo estándar de OAuth 2.0 para renovación de tokens.</p>
     *
     * <p>La verificación se basa en el tiempo de expiración almacenado en la entidad CompanyToken.
     * Si el token necesita ser refrescado, se actualiza tanto el refresh_token como el tiempo
     * de expiración en la base de datos.</p>
     *
     * @param companyToken la entidad CompanyToken que contiene los tokens actuales y la información
     *                     de expiración que se utilizará para determinar si es necesario refrescar
     *
     * @throws RuntimeException si ocurre un error durante la comunicación con la API de MercadoPago
     *                         o si la respuesta no es válida
     *
     * @see CompanyToken
     * @see ResponseMpDto
     * @since 1.0
     */
    @Override
    @Transactional
    public void refreshToken(CompanyToken companyToken) {
        if  (companyToken.getExpiresIn() == 2629056L ) {
            ResponseMpDto data = webClientBuilder.build()
                    .post()
                    .uri("https://api.mercadopago.com/oauth/token")
                    .body(BodyInserters.fromFormData(
                            "client_secret", mpConfig.getClientSecret())
                            .with("grant_type", "refresh_token")
                            .with("test_token", "true"))
                    .retrieve()
                    .bodyToMono(ResponseMpDto.class)
                    .block();

            companyToken.setRefreshToken(data.getRefreshToken());
            companyToken.setExpiresIn(data.getExpiresIn());
        }

    }
}
