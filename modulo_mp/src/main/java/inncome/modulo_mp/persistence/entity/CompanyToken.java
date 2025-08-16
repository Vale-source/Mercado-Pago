package inncome.modulo_mp.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompanyToken {

    // Este ID deberia ser el de la empresa,
    // este modulo debe comunicarse con el de establecimiento y
    // obtener el id por una llamada, luego,
    // en el modulo de mercado pago se almacenan valores de los tokens, etc
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mercadopago_id")
    private String mercadoPagoId; // Id de mercadopago de la cuenta

    @Column(name = "company")
    private String name; // Nombre de la compañia

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "token_expiration_date")
    private Long expiresIn;

    @Column(name = "public_key")
    private String publicKey = null;

    @Column(name = "code_verifier")
    private String codeVerifier; // Codigo verificador que se necesita para el access token

}
