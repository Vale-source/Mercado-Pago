package inncome.modulo_mp.persistence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Información del pagador para procesar el pago")
public class PayerDto {

    @NotBlank
    @Schema(
            description = "Email del comprador. Debe ser una dirección de correo válida",
            example = "juan.perez@email.com",
            required = true,
            format = "email"
    )
    private String email;

    @NotBlank
    @Schema(
            description = "Datos de identificación del comprador (documento, tipo, etc.)",
            required = true,
            implementation = IdentificationDto.class
    )
    private IdentificationDto identification;
}
