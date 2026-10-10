package br.itb.projeto.shieldpme.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDTO {

    private AuthDTO() {
    }

    public record LoginRequest(
            @NotBlank(message = "Informe seu email.") String email,
            @NotBlank(message = "Informe sua senha.") String senha) {
    }

    public record CadastroRequest(
            @NotBlank(message = "Informe seu nome.")
            @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres.") String nome,

            @NotBlank(message = "Informe seu email.")
            @Email(message = "Informe um email válido.")
            @Size(max = 254, message = "O email pode ter no máximo 254 caracteres.") String email,

            @NotBlank(message = "Informe uma senha.")
            @Size(min = 6, max = 64, message = "A senha deve ter entre 6 e 64 caracteres.") String senha) {
    }

    public record UsuarioResumo(String nome, String email) {
    }
}
