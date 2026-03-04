"use client";

import { createContext, useCallback, useContext, useMemo, useState } from "react";

import {
  Dashboard,
  Distribuicao,
  Lote,
  Transferencia,
  logisticaApi,
} from "@/services/logistica-api";

type LogisticaContextType = {
  dashboard: Dashboard | null;
  lotes: Lote[];
  distribuicoes: Distribuicao[];
  transferencias: Transferencia[];
  loading: boolean;
  error: string | null;
  refreshAll: () => Promise<void>;
  entradaLote: (payload: {
    medicamento: string;
    codigoLote: string;
    validade: string;
    quantidade: number;
    local: string;
  }) => Promise<void>;
  criarDistribuicao: (payload: {
    origem: string;
    destino: string;
    medicamento: string;
    quantidade: number;
  }) => Promise<void>;
  expedirDistribuicao: (id: string) => Promise<void>;
  receberDistribuicao: (id: string) => Promise<void>;
  criarTransferencia: (payload: {
    origem: string;
    destino: string;
    medicamento: string;
    quantidade: number;
  }) => Promise<void>;
  enviarTransferencia: (id: string) => Promise<void>;
  receberTransferencia: (id: string, quantidadeRecebida?: number) => Promise<void>;
};

const LogisticaContext = createContext<LogisticaContextType | undefined>(undefined);

export function LogisticaProvider({ children }: { children: React.ReactNode }) {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [distribuicoes, setDistribuicoes] = useState<Distribuicao[]>([]);
  const [transferencias, setTransferencias] = useState<Transferencia[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAction = useCallback(async (action: () => Promise<void>) => {
    try {
      setLoading(true);
      setError(null);
      await action();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro inesperado");
    } finally {
      setLoading(false);
    }
  }, []);

  const refreshAll = useCallback(async () => {
    await handleAction(async () => {
      const [d, l, dist, trans] = await Promise.all([
        logisticaApi.dashboard(),
        logisticaApi.lotes(),
        logisticaApi.distribuicoes(),
        logisticaApi.transferencias(),
      ]);
      setDashboard(d);
      setLotes(l);
      setDistribuicoes(dist);
      setTransferencias(trans);
    });
  }, [handleAction]);

  const entradaLote = useCallback(
    async (payload: {
      medicamento: string;
      codigoLote: string;
      validade: string;
      quantidade: number;
      local: string;
    }) => {
      await handleAction(async () => {
        await logisticaApi.entradaLote(payload);
        await refreshAll();
      });
    },
    [handleAction, refreshAll]
  );

  const criarDistribuicao = useCallback(
    async (payload: {
      origem: string;
      destino: string;
      medicamento: string;
      quantidade: number;
    }) => {
      await handleAction(async () => {
        await logisticaApi.criarDistribuicao(payload);
        await refreshAll();
      });
    },
    [handleAction, refreshAll]
  );

  const expedirDistribuicao = useCallback(
    async (id: string) => {
      await handleAction(async () => {
        await logisticaApi.expedirDistribuicao(id);
        await refreshAll();
      });
    },
    [handleAction, refreshAll]
  );

  const receberDistribuicao = useCallback(
    async (id: string) => {
      await handleAction(async () => {
        await logisticaApi.receberDistribuicao(id);
        await refreshAll();
      });
    },
    [handleAction, refreshAll]
  );

  const criarTransferencia = useCallback(
    async (payload: {
      origem: string;
      destino: string;
      medicamento: string;
      quantidade: number;
    }) => {
      await handleAction(async () => {
        await logisticaApi.criarTransferencia(payload);
        await refreshAll();
      });
    },
    [handleAction, refreshAll]
  );

  const enviarTransferencia = useCallback(
    async (id: string) => {
      await handleAction(async () => {
        await logisticaApi.enviarTransferencia(id);
        await refreshAll();
      });
    },
    [handleAction, refreshAll]
  );

  const receberTransferencia = useCallback(
    async (id: string, quantidadeRecebida?: number) => {
      await handleAction(async () => {
        await logisticaApi.receberTransferencia(id, quantidadeRecebida);
        await refreshAll();
      });
    },
    [handleAction, refreshAll]
  );

  const value = useMemo(
    () => ({
      dashboard,
      lotes,
      distribuicoes,
      transferencias,
      loading,
      error,
      refreshAll,
      entradaLote,
      criarDistribuicao,
      expedirDistribuicao,
      receberDistribuicao,
      criarTransferencia,
      enviarTransferencia,
      receberTransferencia,
    }),
    [
      dashboard,
      lotes,
      distribuicoes,
      transferencias,
      loading,
      error,
      refreshAll,
      entradaLote,
      criarDistribuicao,
      expedirDistribuicao,
      receberDistribuicao,
      criarTransferencia,
      enviarTransferencia,
      receberTransferencia,
    ]
  );

  return <LogisticaContext.Provider value={value}>{children}</LogisticaContext.Provider>;
}

export function useLogistica() {
  const context = useContext(LogisticaContext);
  if (!context) {
    throw new Error("useLogistica deve ser usado dentro de LogisticaProvider");
  }
  return context;
}
