package br.itb.projeto.shieldpme.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.shieldpme.dto.ErroDTO;

/**
 * Converte os erros da API para { "mensagem": "..." }, que é o que o front exibe ao usuário.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    // Erros "de negócio" lançados pelos services (404, 409, 401...)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroDTO> tratarStatus(ResponseStatusException ex) {
        String mensagem = ex.getReason() != null ? ex.getReason() : "Não foi possível concluir a operação.";
        return ResponseEntity.status(ex.getStatusCode()).body(new ErroDTO(mensagem));
    }

    // Falha nas validações (@NotBlank, @Email...): mostra a primeira mensagem
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage())
                .findFirst()
                .orElse("Dados inválidos.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroDTO(mensagem));
    }

    // JSON malformado ou com tipo errado (ex.: data inválida)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroDTO> tratarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErroDTO("Requisição inválida. Confira os dados enviados."));
    }
}
