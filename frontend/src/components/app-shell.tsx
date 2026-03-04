"use client";

import type { ElementType } from "react";

import { Bell, Boxes, SendHorizontal, Truck } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

type Modulo = "estoque" | "distribuicao" | "transferencia";

const navItems: { id: Modulo; label: string; icon: ElementType }[] = [
  { id: "estoque", label: "Estoque", icon: Boxes },
  { id: "distribuicao", label: "Distribuição", icon: SendHorizontal },
  { id: "transferencia", label: "Transferência", icon: Truck },
];

type AppShellProps = {
  moduloAtivo: Modulo;
  onTrocarModulo: (modulo: Modulo) => void;
  notificacoes: number;
  children: React.ReactNode;
};

export function AppShell({ moduloAtivo, onTrocarModulo, notificacoes, children }: AppShellProps) {
  return (
    <div className="grid min-h-screen grid-cols-1 bg-muted/30 lg:grid-cols-[260px_1fr]">
      <aside className="border-r border-border bg-card p-4">
        <div className="mb-6 space-y-1">
          <h1 className="text-xl font-bold">Sisfarma</h1>
          <p className="text-sm text-muted-foreground">Gestão logística municipal</p>
        </div>

        <nav className="space-y-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const ativo = item.id === moduloAtivo;
            return (
              <button
                key={item.id}
                type="button"
                onClick={() => onTrocarModulo(item.id)}
                className={cn(
                  "flex w-full items-center gap-3 rounded-lg px-3 py-2 text-left text-sm font-medium transition-colors",
                  ativo ? "bg-primary text-primary-foreground" : "hover:bg-accent"
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </button>
            );
          })}
        </nav>
      </aside>

      <main>
        <header className="flex h-16 items-center justify-between border-b border-border bg-background px-6">
          <div>
            <h2 className="text-base font-semibold capitalize">Módulo de {moduloAtivo}</h2>
          </div>
          <div className="flex items-center gap-2">
            <Bell className="h-4 w-4 text-muted-foreground" />
            <Badge>{notificacoes} alertas</Badge>
          </div>
        </header>

        <section className="p-6">{children}</section>
      </main>
    </div>
  );
}
