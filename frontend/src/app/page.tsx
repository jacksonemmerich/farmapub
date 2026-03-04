"use client";

import { useEffect, useMemo, useState } from "react";

import { AppShell } from "@/components/app-shell";
import { Card, CardContent } from "@/components/ui/card";
import { LogisticaProvider, useLogistica } from "@/context/logistica-context";
import { DistribuicaoModule } from "@/modules/admin/distribuicao-module";
import { EstoqueModule } from "@/modules/admin/estoque-module";
import { TransferenciaModule } from "@/modules/admin/transferencia-module";

type Modulo = "estoque" | "distribuicao" | "transferencia";

function HomeContent() {
  const { dashboard, error, loading, refreshAll } = useLogistica();
  const [modulo, setModulo] = useState<Modulo>("estoque");

  useEffect(() => {
    refreshAll();
  }, [refreshAll]);

  const notificacoes = useMemo(() => {
    if (!dashboard) {
      return 0;
    }
    return dashboard.lotesCriticos + dashboard.distribuicoesPendentes + dashboard.transferenciasPendentes;
  }, [dashboard]);

  return (
    <AppShell moduloAtivo={modulo} onTrocarModulo={setModulo} notificacoes={notificacoes}>
      {loading && <p className="mb-3 text-sm text-muted-foreground">Atualizando dados...</p>}
      {error && (
        <Card className="mb-4 border-red-300 bg-red-50 text-red-700">
          <CardContent className="pt-4">{error}</CardContent>
        </Card>
      )}

      {modulo === "estoque" && <EstoqueModule />}
      {modulo === "distribuicao" && <DistribuicaoModule />}
      {modulo === "transferencia" && <TransferenciaModule />}
    </AppShell>
  );
}

export default function Home() {
  return (
    <LogisticaProvider>
      <HomeContent />
    </LogisticaProvider>
  );
}
