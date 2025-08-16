package inncome.modulo_mp.service.serviceImpl;

import com.mercadopago.client.oauth.OauthClient;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import inncome.modulo_mp.config.MPConfig;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.persistence.repository.CompanyTokenRepository;
import inncome.modulo_mp.service.OAuthService;
import inncome.modulo_mp.utils.PKCEUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.compress.harmony.pack200.PackingUtils.log;

@Service
public class OAuthServiceImpl implements OAuthService {

    @Autowired
    private MPConfig mpConfig;

    @Autowired
    private CompanyTokenRepository companyTokenRepository;

    /**
     * Genera la URL de autorización OAuth para MercadoPago utilizando el flujo PKCE.
     *
     * <p>Este metodo inicia el proceso de autorización OAuth 2.0 con MercadoPago implementando
     * PKCE (Proof Key for Code Exchange) para mayor seguridad. Genera los códigos necesarios
     * (code_verifier y code_challenge), los almacena en la entidad CompanyToken y construye
     * la URL de autorización que debe ser utilizada para redirigir al usuario a MercadoPago.</p>
     *
     * <p>El flujo incluye:</p>
     * <ul>
     *   <li>Generación del code_verifier aleatorio</li>
     *   <li>Creación del code_challenge usando SHA256</li>
     *   <li>Almacenamiento del code_verifier en la base de datos</li>
     *   <li>Construcción de la URL con los parámetros OAuth necesarios</li>
     * </ul>
     *
     * @param companyTokenId el ID de la empresa para la cual se genera la autorización.
     *                       Se utiliza como state parameter para mantener el contexto.
     *                       Si bien el parametro dice que es opcional, no lo es, solo lleva
     *                       Optional debido al tipado de Java para respetar tipos
     *
     * @return URI que contiene la URL de autorización completa de MercadoPago con todos los
     *         parámetros OAuth necesarios (client_id, redirect_uri, code_challenge, state)
     *
     * @throws NoSuchAlgorithmException si el algoritmo SHA256 no está disponible para generar el code_challenge
     * @throws MPException si ocurre un error en la configuración o comunicación con MercadoPago
     * @throws MPApiException si hay un error específico de la API de MercadoPago
     * @throws RuntimeException si no se encuentra la CompanyToken con el ID proporcionado
     *
     * @see PKCEUtil#generateCodeVerifier()
     * @see PKCEUtil#generateCodeChallenge(String)
     * @see CompanyToken
     * @since 1.0
     */
    @Override
    public URI generateAuth(Optional<Long> companyTokenId) throws NoSuchAlgorithmException, MPException, MPApiException {
        String codeVerifier = PKCEUtil.generateCodeVerifier();
        String codeChallenge = PKCEUtil.generateCodeChallenge(codeVerifier);
        CompanyToken companyToken = companyTokenRepository.findCompanyTokenById(companyTokenId).orElseThrow();
        String state = companyToken.getId().toString();

        companyToken.setCodeVerifier(codeVerifier);
        companyTokenRepository.save(companyToken);

        System.out.println("codeVerifier " + codeVerifier);
        System.out.println("codeChallenge " + codeChallenge);

        String url = new OauthClient().getAuthorizationURL(
                mpConfig.getClientId(),
                mpConfig.getRedirectUri(),
                MPRequestOptions.createDefault());

        return UriComponentsBuilder.fromUriString(url)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .queryParam("state", state)
                .build(true)
                .toUri();
    }
}
