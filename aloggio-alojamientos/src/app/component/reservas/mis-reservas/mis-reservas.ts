import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';

type Filtro = 'TODAS' | 'CONFIRMADA' | 'COMPLETADA' | 'CANCELADA';

export interface ReservaVista {
  idReserva: number;
  idPublicacion: number;
  estado: 'CONFIRMADA' | 'COMPLETADA' | 'CANCELADA';
  fechaCreacion: string;
  fechaLlegada: string;
  fechaSalida: string;
  total: number;
  titulo: string;
  ciudad: string;
  imagen: string | null;
  noches: number;
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
  readonly filtros: { valor: Filtro; etiqueta: string }[] = [
    { valor: 'TODAS', etiqueta: 'Todas' },
    { valor: 'CONFIRMADA', etiqueta: 'Confirmadas' },
    { valor: 'COMPLETADA', etiqueta: 'Completadas' },
    { valor: 'CANCELADA', etiqueta: 'Canceladas' },
  ];
  readonly etiquetasEstado = ETIQUETAS_ESTADO;

  filtroActivo = signal<Filtro>('TODAS');
  cargando = signal(false);
  error = signal<any | null>(null);

  lista = signal<ReservaVista[]>([
    {
      idReserva: 1042,
      idPublicacion: 1,
      estado: 'CONFIRMADA',
      fechaCreacion: '2026-11-01',
      fechaLlegada: '2026-12-10',
      fechaSalida: '2026-12-15',
      total: 2835000,
      titulo: 'Villa Moderna Vista al Valle y Montaña',
      ciudad: 'Medellín',
      imagen:
        'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
      noches: 5,
    },
    {
      idReserva: 1035,
      idPublicacion: 2,
      estado: 'COMPLETADA',
      fechaCreacion: '2026-09-15',
      fechaLlegada: '2026-10-01',
      fechaSalida: '2026-10-05',
      total: 1450000,
      titulo: 'Cabaña Alpina en el Bosque',
      ciudad: 'Guatapé',
      imagen:
        'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=800&q=80',
      noches: 4,
    },
    {
      idReserva: 1010,
      idPublicacion: 3,
      estado: 'CANCELADA',
      fechaCreacion: '2026-08-10',
      fechaLlegada: '2026-08-20',
      fechaSalida: '2026-08-22',
      total: 800000,
      titulo: 'Loft Urbano Minimalista',
      ciudad: 'Bogotá',
      imagen: null,
      noches: 2,
    },
  ]);

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

  ngOnInit(): void {}

  cargar(): void {
    this.cargando.set(true);
    setTimeout(() => {
      this.cargando.set(false);
    }, 400);
  }

  seleccionarFiltro(filtro: Filtro): void {
    this.filtroActivo.set(filtro);
  }

  etiquetaFiltroActivo(): string {
    return this.filtros.find((f) => f.valor === this.filtroActivo())?.etiqueta.toLowerCase() ?? '';
  }
}
