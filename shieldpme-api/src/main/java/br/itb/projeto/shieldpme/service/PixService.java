package br.itb.projeto.shieldpme.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.shieldpme.dto.AssinaturaDTO.PixResponse;
import br.itb.projeto.shieldpme.model.entity.Plano;
import br.itb.projeto.shieldpme.model.repository.PlanoRepository;

@Service
public class PixService {

    private final PlanoRepository planoRepository;

    public PixService(PlanoRepository planoRepository) {
        this.planoRepository = planoRepository;
    }

    /** O valor vem do banco (preço do plano), nunca do front. */
    @Transactional(readOnly = true)
    public PixResponse gerar(String planoCodigo) {
        Plano plano = planoRepository.findByCodigoAndAtivoTrue(planoCodigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado."));

        String copiaECola = BrCodePix.gerar(plano.getPrecoMensal());

        // O front monta o QR Code com "qrCodeData"; usamos o próprio código copia e cola.
        return new PixResponse(copiaECola, copiaECola);
    }
}
