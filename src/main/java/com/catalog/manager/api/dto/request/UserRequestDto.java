package com.catalog.manager.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRequestDto {
    @NotNull(message = "O nome não pode ser null")
    @NotBlank(message = "O nome não pode ser vazio.")
    private String name;

    @NotNull(message = "O email não pode ser null")
    @NotBlank(message = "O email não pode ser vazio.")
    @Email(message = "O email é inválido")
    private String email;

    @NotNull(message = "A senha email não pode ser null")
    @NotBlank(message = "A senha não pode ser vazia.")
    @Size(min= 8, message = "A senha deve ter no mínimo 8 caracteres.")
    @Size(max = 15, message = "senha deve ter no máximo 15 caracteres.")
    @Pattern(
            regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).+$",
            message = "A senha deve conter letra maiúscula, minúscula, número e um dos caracteres especiais: @#$%^&+=!"
    )
    private String password;
}
