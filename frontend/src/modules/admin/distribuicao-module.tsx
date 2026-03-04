"use client";

import { FormEvent, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useLogistica } from "@/context/logistica-context";

const destinos = ["UBS_CENTRO", "UBS_NORTE", "UPA_ZONA_SUL"];

export function DistribuicaoModule() {
  const { distribuicoes, criarDistribuicao, expedirDistribuicao, receberDistribuicao, loading } = useLogistica();

  const [origem, setOrigem] = useState("ESTOQUE_CENTRAL");
  const [destino, setDestino] = useState(destinos[0]);
  const [medicamento, setMedicamento] = useState("Dipirona 500mg");
  const [quantidade, setQuantidade] = useState(0);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await criarDistribuicao({ origem, destino, medicamento, quantidade });
    setQuantidade(0);
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Novo pedido de distribuição</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="grid gap-3 md:grid-cols-4" onSubmit={onSubmit}>
            <Input value={origem} onChange={(e) => setOrigem(e.target.value)} placeholder="Origem" />
            <select
              className="h-9 rounded-md border border-input bg-background px-3 text-sm"
              value={destino}
              onChange={(e) => setDestino(e.target.value)}
            >
              {destinos.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
            <Input value={medicamento} onChange={(e) => setMedicamento(e.target.value)} placeholder="Medicamento" />
            <Input
              type="number"
              min={1}
              value={quantidade || ""}
              onChange={(e) => setQuantidade(Number(e.target.value))}
              placeholder="Quantidade"
              required
            />
            <Button className="md:col-span-4" type="submit" disabled={loading}>
              Gerar distribuição FEFO
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Distribuições</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Origem</TableHead>
                <TableHead>Destino</TableHead>
                <TableHead>Medicamento</TableHead>
                <TableHead>Qtd.</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {distribuicoes.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.origem}</TableCell>
                  <TableCell>{item.destino}</TableCell>
                  <TableCell>{item.medicamento}</TableCell>
                  <TableCell>{item.quantidadeSolicitada}</TableCell>
                  <TableCell>{item.status}</TableCell>
                  <TableCell className="flex flex-wrap gap-2">
                    <Button
                      variant="outline"
                      onClick={() => expedirDistribuicao(item.id)}
                      disabled={loading || item.status !== "RASCUNHO"}
                    >
                      Expedir
                    </Button>
                    <Button
                      onClick={() => receberDistribuicao(item.id)}
                      disabled={loading || item.status !== "EXPEDIDA"}
                    >
                      Receber
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {distribuicoes.map((item) => (
            <div key={`${item.id}-alocacoes`} className="rounded-md border border-border p-3">
              <p className="mb-2 text-sm font-medium">Alocação FEFO de {item.medicamento}</p>
              <ul className="space-y-1 text-sm text-muted-foreground">
                {item.alocacoes.map((alocacao) => (
                  <li key={`${item.id}-${alocacao.loteId}`}>
                    Lote {alocacao.codigoLote} • {alocacao.quantidadeSeparada} un • validade{" "}
                    {new Date(alocacao.validade).toLocaleDateString("pt-BR")}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
}
