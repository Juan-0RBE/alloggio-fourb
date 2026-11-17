import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ReservaService } from '../../../service/reserva.service';
import { EstadoReserva, ReservaDTO } from '../../../model/reserva.model';

type Filtro = 'TODAS' | 'CONFIRMADA' | 'COMPLETADA' | 'CANCELADA';

export interface ReservaVista {
  idReserva: number;
  idPublicacion: number;
  estado: EstadoReserva;
  fechaCreacion: string;
  fechaLlegada: string;
  fechaSalida: string;
  total: number;
  titulo: string;
  ciudad: string;
  imagen: string | null;
  noches: number;
  tieneResena: boolean;
}

/** Convierte el DTO del back en lo que muestran las pantallas; descarta filas incompletas. */
export function aReservaVista(r: ReservaDTO): ReservaVista | null {
  if (!r.idReserva || !r.fechaLlegada || !r.fechaSalida) return null;
  const estados: EstadoReserva[] = ['CONFIRMADA', 'COMPLETADA', 'CANCELADA'];
  const estado = estados.includes(r.estado as EstadoReserva)
    ? (r.estado as EstadoReserva)
    : 'CONFIRMADA';
  return {
    idReserva: r.idReserva,
    idPublicacion: r.idPublicacion ?? 0,
    estado,
    fechaCreacion: r.fechaCreacion ?? '',
    fechaLlegada: r.fechaLlegada,
    fechaSalida: r.fechaSalida,
    total: Number(r.total ?? 0),
    titulo: r.tituloPublicacion || `Alojamiento #${r.idPublicacion ?? ''}`,
    ciudad: r.ciudadPublicacion ?? '',
    imagen: r.urlImagenPrincipal ?? null,
    noches: nochesEntre(r.fechaLlegada, r.fechaSalida),
    tieneResena: !!r.tieneResena,
  };
}

function nochesEntre(llegada: string, salida: string): number {
  const [a1, m1, d1] = llegada.split('-').map(Number);
  const [a2, m2, d2] = salida.split('-').map(Number);
  const ms = new Date(a2, m2 - 1, d2).getTime() - new Date(a1, m1 - 1, d1).getTime();
  return Math.max(0, Math.round(ms / 86400000));
}

export const ETIQUETAS_ESTADO: Record<string, string> = {
  CONFIRMADA: 'Confirmada',
  COMPLETADA: 'Completada',
  CANCELADA: 'Cancelada',
};

@Component({
  selector: 'app-mis-reservas',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './mis-reservas.html',
  styleUrl: './mis-reservas.scss',
})
export class MisReservas implements OnInit {
  private reservaService = inject(ReservaService);

  readonly filtros: { valor: Filtro; etiqueta: string }[] = [
    { valor: 'TODAS', etiqueta: 'Todas' },
    { valor: 'CONFIRMADA', etiqueta: 'Confirmadas' },
    { valor: 'COMPLETADA', etiqueta: 'Completadas' },
    { valor: 'CANCELADA', etiqueta: 'Canceladas' },
  ];
  readonly etiquetasEstado = ETIQUETAS_ESTADO;

  filtroActivo = signal<Filtro>('TODAS');
  cargando = signal(false);
  error = signal<{ mensaje: string; sesion: boolean } | null>(null);

  lista = signal<ReservaVista[]>([]);

  visibles = computed(() => {
    const filtro = this.filtroActivo();
    return filtro === 'TODAS' ? this.lista() : this.lista().filter((r) => r.estado === filtro);
  });

  conteos = computed(() => {
    const total: Record<Filtro, number> = { TODAS: 0, CONFIRMADA: 0, COMPLETADA: 0, CANCELADA: 0 };
    for (const reserva of this.lista()) {
      total.TODAS++;
      total[reserva.estado]++;
    }
    return total;
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.error.set(null);
    this.cargando.set(true);
    this.reservaService.obtenerHistorial().subscribe({
      next: (reservas) => {
        this.lista.set(reservas.map(aReservaVista).filter((r): r is ReservaVista => !!r));
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        if (err.status === 401 || err.status === 403) {
          this.error.set({ mensaje: 'Tu sesión expiró. Inicia sesión de nuevo.', sesion: true });
        } else if (err.status === 0) {
          this.error.set({ mensaje: 'No hay conexión con el servidor.', sesion: false });
        } else {
          this.error.set({ mensaje: 'No pudimos cargar tus reservas.', sesion: false });
        }
      },
    });
  }

  seleccionarFiltro(filtro: Filtro): void {
    this.filtroActivo.set(filtro);
  }

  etiquetaFiltroActivo(): string {
    return this.filtros.find((f) => f.valor === this.filtroActivo())?.etiqueta.toLowerCase() ?? '';
  }
}
