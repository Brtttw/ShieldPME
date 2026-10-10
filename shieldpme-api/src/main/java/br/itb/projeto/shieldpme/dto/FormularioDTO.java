package br.itb.projeto.shieldpme.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public final class FormularioDTO {

    private FormularioDTO() {
    }

    public record ContatoRequest(
            @NotBlank(message = "Informe seu nome.")
            @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres.") String nome,

            @NotBlank(message = "Informe seu email.")
            @Email(message = "Informe um email válido.")
            @Size(max = 254, message = "O email pode ter no máximo 254 caracteres.") String email,

            @NotBlank(message = "Informe o assunto.")
            @Size(max = 200, message = "O assunto pode ter no máximo 200 caracteres.") String assunto,

            @NotBlank(message = "Escreva sua mensagem.")
            @Size(max = 2000, message = "A mensagem pode ter no máximo 2000 caracteres.") String mensagem) {
    }

    public record NewsletterRequest(
            @NotBlank(message = "Informe seu email.")
            @Email(message = "Informe um email válido.")
            @Size(max = 254, message = "O email pode ter no máximo 254 caracteres.") String email,

            @Past(message = "A data de nascimento deve estar no passado.") LocalDate dataNascimento) {
    }
}
