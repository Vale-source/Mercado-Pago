package inncome.modulo_mp.controller;

import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import inncome.modulo_mp.persistence.entity.CompanyToken;
import inncome.modulo_mp.service.serviceImpl.MercadoPagoAuthServiceImpl;
import inncome.modulo_mp.service.serviceImpl.OAuthServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@RequestMapping("/oauth")
@Tag(name = "Autenticación OAuth", description = "Endpoints para la autenticación OAuth con MercadoPago")
public class AuthController {

    @Autowired
    private OAuthServiceImpl oAuthService;

    @Autowired
    private MercadoPagoAuthServiceImpl authService;

    /**
     * Este endpoint es el que se le manda inicialmente a la empresa
     * para el split payment. Se generan los codigos para mandar a
     * MP y MP lo manda a Auht0. MP devuelve el codgio de autorizacion y
     * un state que seria un identificador unico.
     * @param companyTokenId Id de la compañia, tendria que estar registrada antes de generarle el pago
     * @return Url de autorizacion
     * @throws NoSuchAlgorithmException
     */
    @Operation(
            summary = "Iniciar proceso de autorización",
            description = "Genera la URL de autorización para que la empresa inicie el proceso de split payment con MercadoPago. " +
                    "Se generan los códigos para mandar a MP y MP lo manda a Auth0. MP devuelve el código de autorización y " +
                    "un state que sería un identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "URL de autorización generada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "https://auth.mercadopago.com/authorization?client_id=...")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID de empresa inválido o faltante",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor al generar la autorización",
                    content = @Content
            )
    })
    @GetMapping("/start/{companyTokenId}")
    public ResponseEntity<String> generateAuth(
            @Parameter(
                    description = "ID de la empresa registrada. La empresa debe estar registrada antes de generarle el pago",
                    required = true,
                    example = "123"
            )
            @PathVariable Optional<Long> companyTokenId
    ) throws NoSuchAlgorithmException {
        try {

            if (companyTokenId.isEmpty()){
                return ResponseEntity.badRequest().build();
            }

            URI url = oAuthService.generateAuth(companyTokenId);

            return ResponseEntity.ok(url.toString());

        } catch (NoSuchAlgorithmException | MPException | MPApiException e) {
            throw new NoSuchAlgorithmException(e.getMessage());
        }
    }

    /**
     * Este seria el endpoint de callback, una vez que se ejecuta el endpoint de inicio
     * y la empresa acepta, se redirige aca y se pasan los parametros generados en el endpoint anterior
     * @param code Codigo de autorizacion que se genera al mandar el code challenge
     * @param state Identificador unico para la transaccion
     * @return
     * @throws Exception
     */
    @Operation(
            summary = "Callback de autorización",
            description = "Endpoint de callback que se ejecuta después del proceso de autorización. " +
                    "Una vez que se ejecuta el endpoint de inicio y la empresa acepta, se redirige aquí " +
                    "y se pasan los parámetros generados en el endpoint anterior."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autorización procesada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Autorizacion completada")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros de callback inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al procesar la autorización",
                    content = @Content
            )
    })
    @GetMapping("/callback")
    public ResponseEntity<String> callback(
            @Parameter(
                    description = "Código de autorización generado por MercadoPago al mandar el code challenge",
                    required = true,
                    example = "TG-507f1f77bcf86cd799439011"
            )
            @RequestParam String code, //Recibe un codigo de autorizacion

            @Parameter(
                    description = "Identificador único para la transacción, en este caso es el id de la empresa",
                    required = true,
                    example = "123"
            )
            @RequestParam String state

    ) throws Exception {
        try {
            authService.changeCodeForToken(code, Optional.of(Long.valueOf(state)));
            return ResponseEntity.ok("Autorizacion completada");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
