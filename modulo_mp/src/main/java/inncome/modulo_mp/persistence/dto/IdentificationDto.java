package inncome.modulo_mp.persistence.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Información de identificación del pagador (documento de identidad)")
public class IdentificationDto {

    @NotBlank
    @Schema(
            description = "Tipo de documento de identidad",
            example = "DNI",
            required = true,
            allowableValues = {"DNI", "CI", "LC", "LE", "Otro"}
    )
    private String type; //Tipo de DNI

    @NotBlank
    @Schema(
            description = "Número del documento de identidad sin puntos ni espacios",
            example = "12345678",
            required = true,
            pattern = "^[0-9]+$"
    )
    private String number; //Numero de documento
}
