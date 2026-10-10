// teste rápido da api com ela no ar. precisa só do node 18+.
//   node scripts/smoke-test.mjs                        direto na api (8080)
//   node scripts/smoke-test.mjs http://localhost:5173  pelo proxy do vite
// grava dados de verdade no banco (emails smoke-...@example.com)

const BASE = (process.argv[2] || 'http://localhost:8080').replace(/\/$/, '');
const rodada = Date.now().toString(36);
const EMAIL = `smoke-${rodada}@example.com`;
const SENHA = 'Senha@123';

let passou = 0;
let falhou = 0;

async function chamar(metodo, caminho, { corpo, headers = {} } = {}) {
  const r = await fetch(BASE + caminho, {
    method: metodo,
    headers: { 'Content-Type': 'application/json', ...headers },
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  });
  const texto = await r.text();
  let json = null;
  try { json = texto ? JSON.parse(texto) : null; } catch { /* nao era json */ }
  return { status: r.status, headers: r.headers, texto, json };
}

function afirmar(condicao, mensagem) {
  if (!condicao) throw new Error(mensagem);
}

function status(r, esperado) {
  afirmar(r.status === esperado, `esperava HTTP ${esperado}, veio ${r.status}. Corpo: ${r.texto.slice(0, 200) || '(vazio)'}`);
}

// erro tem que vir como { mensagem }, que é o que o front mostra
function erro(r, esperado) {
  status(r, esperado);
  afirmar(typeof r.json?.mensagem === 'string' && r.json.mensagem, `erro sem { mensagem }: ${r.texto.slice(0, 200)}`);
}

async function teste(nome, fn) {
  try {
    await fn();
    passou++;
    console.log(`  ok     ${nome}`);
  } catch (e) {
    falhou++;
    console.log(`  FALHOU ${nome}\n         ${e.message}`);
  }
}

function crc16(texto) {
  let crc = 0xffff;
  for (const b of Buffer.from(texto, 'ascii')) {
    crc ^= b << 8;
    for (let i = 0; i < 8; i++) crc = crc & 0x8000 ? ((crc << 1) ^ 0x1021) & 0xffff : (crc << 1) & 0xffff;
  }
  return crc.toString(16).toUpperCase().padStart(4, '0');
}

const cliente = (s) => ({ nome: 'Cliente Teste', email: `smoke-${rodada}-${s}@example.com`, telefone: '11999998888', cpf: '12345678901', empresa: 'Empresa Teste' });

console.log(`smoke test -> ${BASE}\n`);

try {
  await chamar('GET', '/api/planos');
} catch (e) {
  console.log(`nao consegui falar com ${BASE} (${e.cause?.code || e.message}). a api esta rodando?`);
  process.exit(2);
}

console.log('home, blog e ferramentas');
let servicos;
await teste('GET /api/planos: 3 planos com beneficios', async () => {
  const r = await chamar('GET', '/api/planos');
  status(r, 200);
  afirmar(r.json.length === 3, `esperava 3 planos, vieram ${r.json.length}`);
  afirmar(r.json.every((p) => typeof p.precoMensal === 'number' && p.beneficios.length > 0), 'preco deve ser numero e beneficios nao pode ser vazio');
});
await teste('GET /api/planos/pro e plano inexistente (404)', async () => {
  const r = await chamar('GET', '/api/planos/pro');
  status(r, 200);
  afirmar(r.json.precoMensal === 161, 'o PRO deveria custar 161');
  erro(await chamar('GET', '/api/planos/inexistente'), 404);
});
await teste('GET /api/servicos: 8', async () => {
  const r = await chamar('GET', '/api/servicos');
  status(r, 200);
  afirmar(r.json.length === 8, `vieram ${r.json.length}`);
  servicos = r.json;
});
await teste('GET /api/blog/posts: 6, data yyyy-MM-dd', async () => {
  const r = await chamar('GET', '/api/blog/posts');
  status(r, 200);
  afirmar(r.json.length === 6 && r.json.every((p) => /^\d{4}-\d{2}-\d{2}$/.test(p.data)), 'posts ou datas fora do esperado');
});
await teste('GET /api/ferramentas: 6, 4 recursos cada', async () => {
  const r = await chamar('GET', '/api/ferramentas');
  status(r, 200);
  afirmar(r.json.length === 6 && r.json.every((f) => f.recursos.length === 4), 'ferramentas ou recursos fora do esperado');
});
await teste('acentos chegaram certos do banco', async () => {
  afirmar(servicos?.some((s) => s.titulo === 'Gestão de Riscos'), 'nao achei "Gestão de Riscos". o sql foi lido como utf-8?');
  afirmar(!/Ã[§£©]/.test(JSON.stringify(servicos)), 'texto com acento corrompido');
});

console.log('\ncadastro e login');
await teste('cadastro valido: 201 com corpo', async () => {
  const r = await chamar('POST', '/api/auth/cadastro', { corpo: { nome: 'Teste Smoke', email: EMAIL, senha: SENHA } });
  status(r, 201);
  afirmar(r.json?.email === EMAIL, `corpo inesperado: ${r.texto}`);
  afirmar(!r.texto.includes('senha'), 'a resposta nao pode ter senha nem hash');
});
await teste('email repetido (inclusive em maiuscula): 409', async () => {
  erro(await chamar('POST', '/api/auth/cadastro', { corpo: { nome: 'X', email: EMAIL.toUpperCase(), senha: SENHA } }), 409);
});
await teste('email invalido: 400', async () => {
  erro(await chamar('POST', '/api/auth/cadastro', { corpo: { nome: 'X', email: 'nao-e-email', senha: SENHA } }), 400);
});
await teste('senha curta: 400', async () => {
  erro(await chamar('POST', '/api/auth/cadastro', { corpo: { nome: 'X', email: `smoke-${rodada}-b@example.com`, senha: '123' } }), 400);
});
await teste('login com senha errada: 401', async () => {
  erro(await chamar('POST', '/api/auth/login', { corpo: { email: EMAIL, senha: 'errada123' } }), 401);
});
await teste('login com email que nao existe: 401', async () => {
  erro(await chamar('POST', '/api/auth/login', { corpo: { email: `ninguem-${rodada}@example.com`, senha: SENHA } }), 401);
});
await teste('login correto: 200 com nome e email', async () => {
  const r = await chamar('POST', '/api/auth/login', { corpo: { email: EMAIL, senha: SENHA } });
  status(r, 200);
  afirmar(r.json?.email === EMAIL && r.json?.nome === 'Teste Smoke', `corpo inesperado: ${r.texto}`);
});

console.log('\ncontato e newsletter');
await teste('contato: 204 sem corpo', async () => {
  const r = await chamar('POST', '/api/contatos', { corpo: { nome: 'Teste', email: EMAIL, assunto: 'Assunto', mensagem: 'Mensagem com acentuação: ação, é, ü.' } });
  status(r, 204);
  afirmar(r.texto === '', '204 nao pode ter corpo');
});
await teste('contato sem assunto: 400', async () => {
  erro(await chamar('POST', '/api/contatos', { corpo: { nome: 'X', email: EMAIL, assunto: '', mensagem: 'oi' } }), 400);
});
await teste('newsletter: 204, repetida tambem 204, sem data (null) tambem', async () => {
  status(await chamar('POST', '/api/newsletter', { corpo: { email: EMAIL, dataNascimento: '1990-05-17' } }), 204);
  status(await chamar('POST', '/api/newsletter', { corpo: { email: EMAIL, dataNascimento: null } }), 204);
  status(await chamar('POST', '/api/newsletter', { corpo: { email: `smoke-${rodada}-n@example.com`, dataNascimento: null } }), 204);
});
await teste('newsletter com email invalido ou data futura: 400', async () => {
  erro(await chamar('POST', '/api/newsletter', { corpo: { email: 'xx', dataNascimento: null } }), 400);
  erro(await chamar('POST', '/api/newsletter', { corpo: { email: `smoke-${rodada}-f@example.com`, dataNascimento: '2999-01-01' } }), 400);
});

console.log('\ncheckout');
await teste('pix do plano PRO: crc valido e valor 161.00', async () => {
  const r = await chamar('POST', '/api/pagamentos/pix', { corpo: { planoCodigo: 'pro' } });
  status(r, 200);
  const c = r.json.copiaECola;
  afirmar(c.startsWith('000201') && c.includes('5406161.00'), 'nao parece o pix do plano PRO');
  afirmar(c.endsWith(crc16(c.slice(0, -4))), 'crc invalido');
  afirmar(!/[ #&%+=]/.test(r.json.qrCodeData), 'qrCodeData tem caractere que quebra a url do qr code');
});
await teste('pix de plano inexistente: 404', async () => {
  erro(await chamar('POST', '/api/pagamentos/pix', { corpo: { planoCodigo: 'xxx' } }), 404);
});
const pagamentos = {
  credito: { metodo: 'credito', parcelas: 3, dados: { cartaoNumero: '4111111111111111', cartaoNome: 'CLIENTE', cartaoValidade: '12/30', cartaoCvv: '123' } },
  debito: { metodo: 'debito', parcelas: 1, dados: { debitoNumero: '4111111111111111', debitoNome: 'CLIENTE', debitoValidade: '12/30', debitoCvv: '123', debitoBanco: 'Banco', debitoAgencia: '0001', debitoConta: '12345-6' } },
  pix: { metodo: 'pix', parcelas: 1, dados: {} },
  boleto: { metodo: 'boleto', parcelas: 1, dados: { boletoCpf: '12345678901', boletoEndereco: 'Rua A, 1', boletoCep: '01001000', boletoCidade: 'Sao Paulo/SP' } },
};
for (const [metodo, pagamento] of Object.entries(pagamentos)) {
  await teste(`assinatura com ${metodo}: 201 com corpo, valor do banco`, async () => {
    const r = await chamar('POST', '/api/assinaturas', { corpo: { planoCodigo: 'pro', cliente: cliente(metodo), pagamento } });
    status(r, 201);
    afirmar(r.json?.id > 0 && r.json.status === 'ATIVA' && r.json.valorMensal === 161, `corpo inesperado: ${r.texto}`);
    afirmar(!r.texto.includes('4111'), 'a resposta nao pode ecoar dados de cartao');
  });
}
await teste('assinatura: plano inexistente 404, metodo invalido 400, sem cpf 400', async () => {
  erro(await chamar('POST', '/api/assinaturas', { corpo: { planoCodigo: 'xxx', cliente: cliente('a'), pagamento: pagamentos.pix } }), 404);
  erro(await chamar('POST', '/api/assinaturas', { corpo: { planoCodigo: 'pro', cliente: cliente('b'), pagamento: { metodo: 'cheque', parcelas: 1, dados: {} } } }), 400);
  erro(await chamar('POST', '/api/assinaturas', { corpo: { planoCodigo: 'pro', cliente: { ...cliente('c'), cpf: '' }, pagamento: pagamentos.pix } }), 400);
});

console.log('\ncors');
await teste('origem http://localhost:5173 liberada (preflight)', async () => {
  const r = await chamar('OPTIONS', '/api/auth/login', {
    headers: { Origin: 'http://localhost:5173', 'Access-Control-Request-Method': 'POST', 'Access-Control-Request-Headers': 'content-type' },
  });
  afirmar(r.status === 200 || r.status === 204, `preflight deveria dar 200/204, deu ${r.status}`);
  afirmar(r.headers.get('access-control-allow-origin') === 'http://localhost:5173', 'faltou Access-Control-Allow-Origin');
});
await teste('POST vindo de 127.0.0.1:5174 liberado, de outro site (403)', async () => {
  const corpo = { email: `smoke-${rodada}-cors@example.com`, dataNascimento: null };
  status(await chamar('POST', '/api/newsletter', { corpo, headers: { Origin: 'http://127.0.0.1:5174' } }), 204);
  status(await chamar('POST', '/api/newsletter', { corpo, headers: { Origin: 'http://evil.example' } }), 403);
});

console.log(`\n${passou} passaram, ${falhou} falharam`);
process.exit(falhou === 0 ? 0 : 1);
