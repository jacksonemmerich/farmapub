"use client";

import { FormEvent, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useLogistica } from "@/context/logistica-context";

const locais = ["ESTOQUE_CENTRAL", "UBS_CENTRO", "UPA_ZONA_SUL"];

export function TransferenciaModule() {
  const { transferencias, criarTransferencia, enviarTransferencia, receberTransferencia, loading } = useLogistica();

  const [origem, setOrigem] = useState("ESTOQUE_CENTRAL");
  const [destino, setDestino] = useState("UBS_CENTRO");
  const [medicamento, setMedicamento] = useState("Dipirona 500mg");
  const [quantidade, setQuantidade] = useState(0);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await criarTransferencia({ origem, destino, medicamento, quantidade });
    setQuantidade(0);
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Nova transferência entre unidades</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="grid gap-3 md:grid-cols-4" onSubmit={onSubmit}>
            <select
              className="h-9 rounded-md border border-input bg-background px-3 text-sm"
              value={origem}
              onChange={(e) => setOrigem(e.target.value)}
            >
              {locais.map((item) => (
                <option key={`origem-${item}`} value={item}>
                  Origem: {item}
                </option>
              ))}
            </select>
            <select
              className="h-9 rounded-md border border-input bg-background px-3 text-sm"
              value={destino}
              onChange={(e) => setDestino(e.target.value)}
            >
              {locais.map((item) => (
                <option key={`destino-${item}`} value={item}>
                  Destino: {item}
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
            <Button className="md:col-span-4" type="submit" disabled={loading || origem === destino}>
              Criar transferência
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Transferências</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Origem</TableHead>
                <TableHead>Destino</TableHead>
                <TableHead>Medicamento</TableHead>
                <TableHead>Solicitado</TableHead>
                <TableHead>Recebido</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {transferencias.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.origem}</TableCell>
                  <TableCell>{item.destino}</TableCell>
                  <TableCell>{item.medicamento}</TableCell>
                  <TableCell>{item.quantidadeSolicitada}</TableCell>
                  <TableCell>{item.quantidadeRecebida ?? "-"}</TableCell>
                  <TableCell>{item.status}</TableCell>
                  <TableCell className="flex flex-wrap gap-2">
                    <Button
                      variant="outline"
                      onClick={() => enviarTransferencia(item.id)}
                      disabled={loading || item.status !== "RASCUNHO"}
                    >
                      Enviar
                    </Button>
                    <Button
                      onClick={() => receberTransferencia(item.id, item.quantidadeSolicitada)}
                      disabled={loading || item.status !== "ENVIADA"}
                    >
                      Receber completo
                    </Button>
                    <Button
                      variant="secondary"
                      onClick={() => receberTransferencia(item.id, Math.max(0, item.quantidadeSolicitada - 1))}
                      disabled={loading || item.status !== "ENVIADA"}
                    >
                      Receber parcial
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
