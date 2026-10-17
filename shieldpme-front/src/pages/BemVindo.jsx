import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import PaginaInterna from '../components/shared/PaginaInterna';
import { sessao } from '../utils/sessao';

// Página de quem acabou de entrar ou de se cadastrar
export default function BemVindo() {
  const navigate = useNavigate();
  const { state } = useLocation();
  const usuario = sessao.obter();

  if (!usuario) return <Navigate to="/login" replace />;

  const cadastro = state?.origem === 'cadastro';

  function sair() {
    sessao.limpar();
    navigate('/');
  }

  return (
    <PaginaInterna
      titulo={`Olá, ${usuario.nome}!`}
      introducao={cadastro ? 'Seu cadastro foi realizado com sucesso.' : 'Você fez login com sucesso.'}
    >
      <div className="account-highlights">
        <div className="account-highlight">
          <h4>Nome</h4>
          <p>{usuario.nome}</p>
        </div>
        <div className="account-highlight">
          <h4>Email</h4>
          <p>{usuario.email}</p>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '16px', marginTop: '32px' }}>
        <Link to="/" className="btn" style={{ textDecoration: 'none', textAlign: 'center' }}>
          Ir para o início
        </Link>
        <button type="button" className="btn" onClick={sair}>
          Sair
        </button>
      </div>
    </PaginaInterna>
  );
}
