const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/logistica";

export type Dashboard = {
  estoqueTotal: number;
  lotesCriticos: number;
  lotesAtencao: number;
  distribuicoesPendentes: number;
  transferenciasPendentes: number;
};

export type Alocacao = {
  loteId: string;
  codigoLote: string;
  validade: string;
  quantidadeSeparada: number;
  local: string;
};

export type Lote = {
  id: string;
  medicamento: string;
  codigoLote: string;
  validade: string;
  quantidade: number;
  local: string;
  status: "OK" | "ATENCAO" | "CRITICO";
};

export type Distribuicao = {
  id: string;
  origem: string;
  destino: string;
  medicamento: string;
  quantidadeSolicitada: number;
  status: string;
  createdAt: string;
  alocacoes: Alocacao[];
};

export type Transferencia = {
  id: string;
  origem: string;
  destino: string;
  medicamento: string;
  quantidadeSolicitada: number;
  quantidadeRecebida: number | null;
  status: string;
  createdAt: string;
  alocacoes: Alocacao[];
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
    const payload = (await response.json().catch(() => ({}))) as { message?: string };
    throw new Error(payload.message || `Erro na requisição: ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export const logisticaApi = {
  dashboard: () => request<Dashboard>("/dashboard"),
  lotes: () => request<Lote[]>("/estoque/lotes"),
  entradaLote: (body: {
    medicamento: string;
    codigoLote: string;
    validade: string;
    quantidade: number;
    local: string;
  }) => request<Lote>("/estoque/entrada", { method: "POST", body: JSON.stringify(body) }),
  distribuicoes: () => request<Distribuicao[]>("/distribuicoes"),
  criarDistribuicao: (body: {
    origem: string;
    destino: string;
    medicamento: string;
    quantidade: number;
  }) => request<Distribuicao>("/distribuicoes", { method: "POST", body: JSON.stringify(body) }),
  expedirDistribuicao: (id: string) => request<Distribuicao>(`/distribuicoes/${id}/expedir`, { method: "POST" }),
  receberDistribuicao: (id: string) => request<Distribuicao>(`/distribuicoes/${id}/receber`, { method: "POST" }),
  transferencias: () => request<Transferencia[]>("/transferencias"),
  criarTransferencia: (body: {
    origem: string;
    destino: string;
    medicamento: string;
    quantidade: number;
  }) => request<Transferencia>("/transferencias", { method: "POST", body: JSON.stringify(body) }),
  enviarTransferencia: (id: string) => request<Transferencia>(`/transferencias/${id}/enviar`, { method: "POST" }),
  receberTransferencia: (id: string, quantidadeRecebida?: number) =>
    request<Transferencia>(`/transferencias/${id}/receber`, {
      method: "POST",
      body: JSON.stringify({ quantidadeRecebida }),
    }),
};
