"use client";

import { FormEvent, useMemo, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useLogistica } from "@/context/logistica-context";

const locais = ["ESTOQUE_CENTRAL", "UBS_CENTRO", "UPA_ZONA_SUL"];

export function EstoqueModule() {
  const { dashboard, lotes, entradaLote, loading } = useLogistica();

  const [medicamento, setMedicamento] = useState("Dipirona 500mg");
  const [codigoLote, setCodigoLote] = useState("");
  const [validade, setValidade] = useState("");
  const [quantidade, setQuantidade] = useState(0);
  const [local, setLocal] = useState(locais[0]);

  const cards = useMemo(
    () => [
      { label: "Estoque total", valor: dashboard?.estoqueTotal ?? 0 },
      { label: "Lotes críticos", valor: dashboard?.lotesCriticos ?? 0 },
      { label: "Lotes atenção", valor: dashboard?.lotesAtencao ?? 0 },
    ],
    [dashboard]
  );

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await entradaLote({ medicamento, codigoLote, validade, quantidade, local });
    setCodigoLote("");
    setValidade("");
    setQuantidade(0);
  }

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-3">
        {cards.map((card) => (
          <Card key={card.label}>
            <CardHeader>
              <CardTitle>{card.label}</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-bold">{card.valor}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Entrada de lote</CardTitle>
        </CardHeader>
        <CardContent>
          <form className="grid gap-3 md:grid-cols-5" onSubmit={onSubmit}>
            <Input value={medicamento} onChange={(e) => setMedicamento(e.target.value)} placeholder="Medicamento" />
            <Input value={codigoLote} onChange={(e) => setCodigoLote(e.target.value)} placeholder="Código lote" required />
            <Input type="date" value={validade} onChange={(e) => setValidade(e.target.value)} required />
            <Input
              type="number"
              min={1}
              value={quantidade || ""}
              onChange={(e) => setQuantidade(Number(e.target.value))}
              placeholder="Quantidade"
              required
            />
            <select
              className="h-9 rounded-md border border-input bg-background px-3 text-sm"
              value={local}
              onChange={(e) => setLocal(e.target.value)}
            >
              {locais.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
            <Button className="md:col-span-5" type="submit" disabled={loading}>
              Registrar entrada
            </Button>
          </form>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Lotes por local</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Medicamento</TableHead>
                <TableHead>Lote</TableHead>
                <TableHead>Validade</TableHead>
                <TableHead>Quantidade</TableHead>
                <TableHead>Local</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {lotes.map((lote) => (
                <TableRow key={lote.id}>
                  <TableCell>{lote.medicamento}</TableCell>
                  <TableCell>{lote.codigoLote}</TableCell>
                  <TableCell>{new Date(lote.validade).toLocaleDateString("pt-BR")}</TableCell>
                  <TableCell>{lote.quantidade}</TableCell>
                  <TableCell>{lote.local}</TableCell>
                  <TableCell>
                    <Badge
                      className={
                        lote.status === "CRITICO"
                          ? "border-red-300 bg-red-100 text-red-700"
                          : lote.status === "ATENCAO"
                            ? "border-amber-300 bg-amber-100 text-amber-700"
                            : "border-emerald-300 bg-emerald-100 text-emerald-700"
                      }
                    >
                      {lote.status}
                    </Badge>
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
