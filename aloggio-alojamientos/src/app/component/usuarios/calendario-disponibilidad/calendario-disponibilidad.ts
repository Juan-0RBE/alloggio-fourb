import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';

type TipoPeriodo = 'BLOQUEADO' | 'RESERVADO';
type EstadoDia = 'LIBRE' | 'BLOQUEADO' | 'RESERVADO' | 'PASADO';

export interface PeriodoVista {
  idPeriodoDisponibilidad: number;
  fechaInicio: string;
  fechaFin: string;
  tipo: TipoPeriodo;
  idReserva: number | null;
}

interface DiaCalendario {
  fecha: string;
  numero: number;
  estado: EstadoDia;
}

export const ETIQUETAS_PERIODO: Record<TipoPeriodo, string> = {
  BLOQUEADO: 'Bloqueado',
  RESERVADO: 'Reservado',
};

@Component({
  selector: 'app-calendario-disponibilidad',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, DatePipe],
  templateUrl: './calendario-disponibilidad.html',
  styleUrl: './calendario-disponibilidad.scss',
})
export class CalendarioDisponibilidad implements OnInit {
  readonly etiquetasPeriodo = ETIQUETAS_PERIODO;
  readonly diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  readonly hoy = this.aIso(new Date());

  idPublicacion = 0;
  titulo = 'Villa Moderna Vista al Valle y Montaña';

  cargando = signal(false);
  error = signal<any | null>(null);
  mensajeConfirmacion = signal('');
  errorFormulario = signal('');

  fechaInicio = '';
  fechaFin = '';

  mesVisible = signal(new Date(new Date().getFullYear(), new Date().getMonth(), 1));

  periodos = signal<PeriodoVista[]>([
    {
      idPeriodoDisponibilidad: 12,
      fechaInicio: '2026-11-25',
      fechaFin: '2026-11-28',
      tipo: 'BLOQUEADO',
      idReserva: null,
    },
    {
      idPeriodoDisponibilidad: 13,
      fechaInicio: '2026-12-10',
      fechaFin: '2026-12-15',
      tipo: 'RESERVADO',
      idReserva: 1042,
    },
    {
      idPeriodoDisponibilidad: 14,
      fechaInicio: '2027-01-05',
      fechaFin: '2027-01-08',
      tipo: 'BLOQUEADO',
      idReserva: null,
    },
  ]);

  periodosOrdenados = computed(() =>
    [...this.periodos()].sort((a, b) => a.fechaInicio.localeCompare(b.fechaInicio)),
  );

  celdas = computed(() => {
    const mes = this.mesVisible();
    const totalDias = new Date(mes.getFullYear(), mes.getMonth() + 1, 0).getDate();
    const desfase = (new Date(mes.getFullYear(), mes.getMonth(), 1).getDay() + 6) % 7;
    const celdas: (DiaCalendario | null)[] = Array(desfase).fill(null);
    for (let numero = 1; numero <= totalDias; numero++) {
      const fecha = this.aIso(new Date(mes.getFullYear(), mes.getMonth(), numero));
      celdas.push({ fecha, numero, estado: this.estadoDelDia(fecha) });
    }
    return celdas;
  });

  constructor(private ruta: ActivatedRoute) {}

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    if (!id) {
      this.error.set({ mensaje: 'No encontramos este alojamiento.', sesion: false });
      return;
    }
    this.idPublicacion = id;
  }

  cargar(): void {
    this.error.set(null);
    this.cargando.set(true);
    setTimeout(() => {
      this.cargando.set(false);
    }, 400);
  }

  mesAnterior(): void {
    const mes = this.mesVisible();
    this.mesVisible.set(new Date(mes.getFullYear(), mes.getMonth() - 1, 1));
  }

  mesSiguiente(): void {
    const mes = this.mesVisible();
    this.mesVisible.set(new Date(mes.getFullYear(), mes.getMonth() + 1, 1));
  }

  bloquearRango(): void {
    this.mensajeConfirmacion.set('');
    const mensaje = this.validarRango();
    this.errorFormulario.set(mensaje);
    if (mensaje) return;

    const siguienteId = Math.max(0, ...this.periodos().map((p) => p.idPeriodoDisponibilidad)) + 1;
    this.periodos.update((lista) => [
      ...lista,
      {
        idPeriodoDisponibilidad: siguienteId,
        fechaInicio: this.fechaInicio,
        fechaFin: this.fechaFin,
        tipo: 'BLOQUEADO',
        idReserva: null,
      },
    ]);
    this.fechaInicio = '';
    this.fechaFin = '';
    this.mensajeConfirmacion.set('Rango bloqueado. Ya no aparece disponible para los huéspedes.');
  }

  desbloquear(periodo: PeriodoVista): void {
    if (periodo.tipo !== 'BLOQUEADO') return;
    this.periodos.update((lista) =>
      lista.filter((p) => p.idPeriodoDisponibilidad !== periodo.idPeriodoDisponibilidad),
    );
    this.mensajeConfirmacion.set('Rango desbloqueado. Las fechas vuelven a estar disponibles.');
  }

  noches(periodo: PeriodoVista): number {
    const inicio = new Date(periodo.fechaInicio + 'T00:00:00');
    const fin = new Date(periodo.fechaFin + 'T00:00:00');
    return Math.round((fin.getTime() - inicio.getTime()) / 86400000);
  }

  private validarRango(): string {
    if (!this.fechaInicio || !this.fechaFin || this.fechaFin <= this.fechaInicio) {
      return 'Elige las dos fechas. La de fin debe ser posterior a la de inicio.';
    }
    if (this.fechaInicio < this.hoy) {
      return 'No puedes bloquear un rango que empieza en el pasado.';
    }
    const cruces = this.periodos().filter(
      (p) => this.fechaInicio < p.fechaFin && this.fechaFin > p.fechaInicio,
    );
    if (cruces.some((p) => p.tipo === 'RESERVADO')) {
      return 'El rango se cruza con una reserva confirmada.';
    }
    if (cruces.length > 0) {
      return 'El rango se cruza con otro bloqueo existente.';
    }
    return '';
  }

  private estadoDelDia(fecha: string): EstadoDia {
    if (fecha < this.hoy) return 'PASADO';
    const periodo = this.periodos().find((p) => fecha >= p.fechaInicio && fecha < p.fechaFin);
    return periodo ? periodo.tipo : 'LIBRE';
  }

  private aIso(fecha: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${fecha.getFullYear()}-${pad(fecha.getMonth() + 1)}-${pad(fecha.getDate())}`;
  }
}
