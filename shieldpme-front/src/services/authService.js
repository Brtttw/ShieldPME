import { api } from './api';
import { USE_MOCK } from '../config/env';

// POST /api/auth/login   { email, senha }  -> { nome, email }
export async function login({ email, senha }) {
  if (USE_MOCK) return null;

  return api.post('/auth/login', { email, senha });
}

// POST /api/auth/cadastro   { nome, email, senha }
export async function cadastrar({ nome, email, senha }) {
  if (USE_MOCK) return null;

  return api.post('/auth/cadastro', { nome, email, senha });
}
