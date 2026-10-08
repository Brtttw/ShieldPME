package br.itb.projeto.shieldpme.dto;

/**
 * Formato de TODOS os erros da API. O front (services/api.js) exibe o campo "mensagem".
 */
public record ErroDTO(String mensagem) {
}
