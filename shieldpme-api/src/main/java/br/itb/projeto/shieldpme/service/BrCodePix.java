package br.itb.projeto.shieldpme.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

/**
 * Monta o texto do "PIX copia e cola" (BR Code estático, padrão EMV do Banco Central).
 *
 * É uma SIMULAÇÃO: a chave abaixo é fictícia (a mesma do mock do front). O código gerado
 * tem formato e CRC válidos, mas nenhum pagamento real chega a lugar algum.
 */
final class BrCodePix {

    private static final String CHAVE_PIX_FICTICIA = "shieldpme@pagamento.com";

    private BrCodePix() {
    }

    static String gerar(BigDecimal valor) {
        String contaDoRecebedor = campo("00", "BR.GOV.BCB.PIX") + campo("01", CHAVE_PIX_FICTICIA);

        String semCrc = campo("00", "01")                                   // versão do formato
                + campo("26", contaDoRecebedor)                            // dados da conta PIX
                + campo("52", "0000")                                      // categoria do comerciante
                + campo("53", "986")                                       // moeda: BRL
                + campo("54", valor.setScale(2, RoundingMode.HALF_UP).toPlainString())
                + campo("58", "BR")
                + campo("59", "ShieldPME")
                + campo("60", "SAOPAULO")
                + campo("62", campo("05", "***"))                          // identificador da transação
                + "6304";                                                  // o CRC (4 dígitos) vem logo depois

        return semCrc + crc16(semCrc);
    }

    /** Formato EMV: ID (2 dígitos) + tamanho (2 dígitos) + valor. */
    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    /** CRC-16/CCITT-FALSE (polinômio 0x1021, valor inicial 0xFFFF), em hexadecimal maiúsculo. */
    static String crc16(String texto) {
        int crc = 0xFFFF;
        for (byte b : texto.getBytes(StandardCharsets.US_ASCII)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
