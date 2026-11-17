import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { DisponibilidadService } from '../../../service/disponibilidad.service';
import { PublicacionService } from '../../../service/publicacion.service';
import { TipoPeriodo } from '../../../model/disponibilidad.model';

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

interface ErrorPagina {
  mensaje: string;
  sesion: boolean;
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
  private ruta = inject(ActivatedRoute);
  private disponibilidadService = inject(DisponibilidadService);
  private publicacionService = inject(PublicacionService);

  readonly etiquetasPeriodo = ETIQUETAS_PERIODO;
  readonly diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];
  readonly hoy = this.aIso(new Date());

  idPublicacion = 0;
  titulo = signal('');

  cargando = signal(false);
  guardando = signal(false);
  error = signal<ErrorPagina | null>(null);
  mensajeConfirmacion = signal('');
  errorFormulario = signal('');

  fechaInicio = '';
  fechaFin = '';

  mesVisible = signal(new Date(new Date().getFullYear(), new Date().getMonth(), 1));

  periodos = signal<PeriodoVista[]>([]);

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

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    if (!id) {
      this.error.set({ mensaje: 'No encontramos este alojamiento.', sesion: false });
      return;
    }
    this.idPublicacion = id;
    this.cargarTitulo();
    this.cargar();
  }

  /** Trae del back los periodos bloqueados y reservados de la publicación. */
  cargar(): void {
    this.error.set(null);
    this.cargando.set(true);
    this.disponibilidadService.consultarCalendario(this.idPublicacion).subscribe({
      next: (lista) => {
        this.periodos.set(
          lista.map((p) => ({
            idPeriodoDisponibilidad: p.idPeriodoDisponibilidad!,
            fechaInicio: p.fechaInicio,
            fechaFin: p.fechaFin,
            tipo: p.tipo ?? 'BLOQUEADO',
            idReserva: p.idReserva ?? null,
          })),
        );
        this.cargando.set(false);
      },
      error: (err) => {
        this.cargando.set(false);
        this.error.set(this.errorDeCarga(err));
      },
    });
  }

  /** El título no viene en el calendario, se busca entre las publicaciones del anfitrión. */
  private cargarTitulo(): void {
    this.publicacionService.obtenerMisPublicaciones().subscribe({
      next: (lista) => {
        const pub = lista.find((p) => p.idPublicacion === this.idPublicacion);
        this.titulo.set(pub?.titulo ?? '');
      },
      error: () => this.titulo.set(''),
    });
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
    if (this.guardando()) return;
    this.mensajeConfirmacion.set('');

    // Validación rápida en el front; el back vuelve a validar
    const mensaje = this.validarRango();
    this.errorFormulario.set(mensaje);
    if (mensaje) return;

    this.guardando.set(true);
    this.disponibilidadService
      .bloquearRango(this.idPublicacion, this.fechaInicio, this.fechaFin)
      .subscribe({
        next: () => {
          this.guardando.set(false);
          this.fechaInicio = '';
          this.fechaFin = '';
          this.mensajeConfirmacion.set(
            'Rango bloqueado. Ya no aparece disponible para los huéspedes.',
          );
          this.cargar();
        },
        error: (err) => {
          this.guardando.set(false);
          this.errorFormulario.set(this.textoError(err, 'No se pudo bloquear el rango.'));
        },
      });
  }

  desbloquear(periodo: PeriodoVista): void {
    if (periodo.tipo !== 'BLOQUEADO' || this.guardando()) return;
    this.mensajeConfirmacion.set('');
    this.errorFormulario.set('');

    this.guardando.set(true);
    this.disponibilidadService.desbloquearRango(periodo.idPeriodoDisponibilidad).subscribe({
      next: () => {
        this.guardando.set(false);
        this.periodos.update((lista) =>
          lista.filter((p) => p.idPeriodoDisponibilidad !== periodo.idPeriodoDisponibilidad),
        );
        this.mensajeConfirmacion.set('Rango desbloqueado. Las fechas vuelven a estar disponibles.');
      },
      error: (err) => {
        this.guardando.set(false);
        this.errorFormulario.set(this.textoError(err, 'No se pudo desbloquear el rango.'));
      },
    });
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

  // Manejo de errores

  private errorDeCarga(err: unknown): ErrorPagina {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 401) return { mensaje: 'Tu sesión expiró. Inicia sesión de nuevo.', sesion: true };
      if (err.status === 403)
        return { mensaje: 'Este alojamiento no pertenece a tu cuenta.', sesion: false };
      if (err.status === 404) return { mensaje: 'No encontramos este alojamiento.', sesion: false };
    }
    return { mensaje: this.textoError(err, 'No se pudo cargar el calendario.'), sesion: false };
  }

  private textoError(err: unknown, porDefecto: string): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 0) return 'No hay conexión con el servidor.';
      if (typeof err.error === 'string' && err.error.trim()) return err.error;
      if (err.error?.message) return err.error.message;
    }
    return porDefecto;
  }
}
