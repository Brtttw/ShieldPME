// Sessão simples: guarda só { nome, email } no navegador (a API não usa token)
const CHAVE_USUARIO = 'shieldpme_usuario';

export const sessao = {
  obter() {
    try {
      return JSON.parse(localStorage.getItem(CHAVE_USUARIO));
    } catch {
      return null;
    }
  },
  salvar: (usuario) => localStorage.setItem(CHAVE_USUARIO, JSON.stringify(usuario)),
  limpar: () => localStorage.removeItem(CHAVE_USUARIO),
};
