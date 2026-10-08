package br.itb.projeto.shieldpme.dto;

import java.math.BigDecimal;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Checkout: PIX e assinatura.
 */
public final class AssinaturaDTO {

    private AssinaturaDTO() {
    }

    /** POST /api/pagamentos/pix */
    public record PixRequest(
            @NotBlank(message = "Informe o plano.") String planoCodigo) {
    }

    /** Resposta do PIX: { copiaECola, qrCodeData } */
    public record PixResponse(String copiaECola, String qrCodeData) {
    }

    /** Dados pessoais do checkout */
    public record Cliente(
            @NotBlank(message = "Informe o nome.")
            @Size(max = 150, message = "O nome pode ter no máximo 150 caracteres.") String nome,

            @NotBlank(message = "Informe o email.")
            @Email(message = "Informe um email válido.")
            @Size(max = 254, message = "O email pode ter no máximo 254 caracteres.") String email,

            @NotBlank(message = "Informe o telefone.")
            @Size(max = 20, message = "O telefone pode ter no máximo 20 caracteres.") String telefone,

            @NotBlank(message = "Informe o CPF.")
            @Size(max = 14, message = "O CPF pode ter no máximo 14 caracteres.") String cpf,

            @NotBlank(message = "Informe o nome da empresa.")
            @Size(max = 150, message = "O nome da empresa pode ter no máximo 150 caracteres.") String empresa) {
    }

    /**
     * Forma de pagamento. "dados" traz número do cartão, CVV etc. conforme o método.
     * É recebido só porque o front envia, mas NUNCA é gravado, logado nem repassado:
     * não guardamos dados de cartão (pagamento simulado).
     */
    public record Pagamento(
            @NotBlank(message = "Informe a forma de pagamento.")
            @Pattern(regexp = "credito|debito|pix|boleto", message = "Forma de pagamento inválida.") String metodo,

            @NotNull(message = "Informe o número de parcelas.")
            @Min(value = 1, message = "Parcelas inválidas.")
            @Max(value = 12, message = "Parcelas inválidas.") Integer parcelas,

            Map<String, String> dados) {
    }

    /** POST /api/assinaturas */
    public record AssinaturaRequest(
            @NotBlank(message = "Informe o plano.") String planoCodigo,
            @NotNull(message = "Informe os dados do cliente.") @Valid Cliente cliente,
            @NotNull(message = "Informe os dados do pagamento.") @Valid Pagamento pagamento) {
    }

    /** Resposta da assinatura (o front só precisa de um JSON válido com status 201). */
    public record AssinaturaResponse(Long id, String planoCodigo, BigDecimal valorMensal, String status) {
    }
}
