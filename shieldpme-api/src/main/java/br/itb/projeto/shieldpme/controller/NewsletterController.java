package br.itb.projeto.shieldpme.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.shieldpme.dto.FormularioDTO.NewsletterRequest;
import br.itb.projeto.shieldpme.service.NewsletterService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/newsletter")
public class NewsletterController {

    private final NewsletterService newsletterService;

    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }

    @PostMapping
    public ResponseEntity<Void> inscrever(@Valid @RequestBody NewsletterRequest req) {
        newsletterService.inscrever(req);
        return ResponseEntity.noContent().build();
    }
}
