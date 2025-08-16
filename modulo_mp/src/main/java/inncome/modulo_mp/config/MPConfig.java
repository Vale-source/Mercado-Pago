package inncome.modulo_mp.config;

import com.mercadopago.MercadoPagoConfig;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.persistence.repository.CompanyTokenRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Getter
public class MPConfig {

    @Autowired
    private CompanyTokenRepository companyTokenRepository;

    @Value("${mercado.pago.access.token}")
    private String accessToken; // Cargo el access token

    @Value("${mercado.pago.client.id}")
    private String clientId; // Cargo el client id

    @Value("${mercado.pago.redirect.uri}")

    private String redirectUri; // Uri de redireccionamiento

    @Value("${mercado.pago.client.secret}")
    private String clientSecret;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }
}
