package inncome.modulo_mp.persistence.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Respuesta de autenticación OAuth de MercadoPago con tokens de acceso")
public class ResponseMpDto {

    @JsonProperty("access_token")
    @Schema(
            description = "Token de acceso para realizar llamadas a la API de MercadoPago",
            example = "APP_USR-1234567890abcdef-112233-44556677889900aabbccddeeff112233-123456789"
    )
    private String accessToken;

    @JsonProperty("token_type")
    @Schema(
            description = "Tipo de token de autorización",
            example = "Bearer",
            allowableValues = {"Bearer"}
    )
    private String tokenType;

    @JsonProperty("expires_in")
    @Schema(
            description = "Tiempo de expiración del token en segundos",
            example = "21600"
    )
    private Long expiresIn;

    @JsonProperty("user_id")
    @Schema(
            description = "ID único del usuario en MercadoPago",
            example = "123456789"
    )
    private String mercadoPagoId;

    @JsonProperty("refresh_token")
    @Schema(
            description = "Token para renovar el access_token cuando expire",
            example = "TG-507f1f77bcf86cd799439011"
    )
    private String refreshToken;

    @JsonProperty("public_key")
    @Schema(
            description = "Clave pública de la aplicación para operaciones del lado del cliente",
            example = "APP_USR-2d816625-27d1-47c6-122d-c297854cbd89"
    )
    private String publicKey;
}
