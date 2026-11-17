import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ReservaService } from '../../../service/reserva.service';
import { NotificacionService } from '../../../service/notificacion.service';
import { ReservaDTO } from '../../../model/reserva.model';
import {
  CancelacionPayload,
  FormularioCancelacion,
  PoliticaVista,
} from '../formulario-cancelacion/formulario-cancelacion';
import { FormularioResena, ResenaPayload } from '../../resenas/formulario-resena/formulario-resena';
import { ResenaService } from '../../../service/resena.service';
import { ETIQUETAS_ESTADO, ReservaVista, aReservaVista } from '../mis-reservas/mis-reservas';

const DESCRIPCIONES_ESTADO: Record<ReservaVista['estado'], string> = {
  CONFIRMADA: 'Tu reserva está confirmada y las fechas están bloqueadas para ti.',
  COMPLETADA: 'Tu estadía terminó. Ya puedes dejar una reseña del alojamiento.',
  CANCELADA: 'Esta reserva fue cancelada y las fechas quedaron libres.',
};

@Component({
  selector: 'app-detalle-reserva',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, FormularioCancelacion, FormularioResena],
  templateUrl: './detalle-reserva.html',
  styleUrl: './detalle-reserva.scss',
})
export class DetalleReserva implements OnInit {
  readonly etiquetasEstado = ETIQUETAS_ESTADO;
  readonly descripcionesEstado = DESCRIPCIONES_ESTADO;

  cargando = signal(false);
  error = signal<any | null>(null);
  mensajeConfirmacion = signal('');

  mostrandoCancelacion = signal(false);
  cancelando = signal(false);
  errorCancelacion = signal('');

  mostrandoResena = signal(false);
  enviandoResena = signal(false);
  errorResena = signal('');

  reserva = signal<ReservaVista | null>(null);
  politica = signal<PoliticaVista | null>(null);

  private ruta = inject(ActivatedRoute);
  private reservaService = inject(ReservaService);
  private notificacionService = inject(NotificacionService);
  private resenaService = inject(ResenaService);
  private idReserva = 0;

  ngOnInit(): void {
    const texto = this.ruta.snapshot.paramMap.get('id') ?? '';
    const id = Number(texto);
    if (!/^[0-9]+$/.test(texto) || id <= 0) {
      this.error.set({ mensaje: 'No encontramos esta reserva.', sesion: false });
      return;
    }
    this.idReserva = id;
    this.cargar();
  }

  cargar(): void {
    if (!this.idReserva) return;
    this.error.set(null);
    this.cargando.set(true);
    this.reservaService.consultarEstado(this.idReserva).subscribe({
      next: (dto) => {
        const vista = aReservaVista(dto);
        if (!vista) {
          this.cargando.set(false);
          this.error.set({ mensaje: 'Los datos de la reserva están incompletos.', sesion: false });
          return;
        }
        this.reserva.set(vista);
        this.politica.set(this.aPolitica(dto));
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        if (err.status === 401) {
          this.error.set({ mensaje: 'Tu sesión expiró. Inicia sesión de nuevo.', sesion: true });
        } else if (err.status === 404 || err.status === 403) {
          this.error.set({ mensaje: 'No encontramos esta reserva en tu cuenta.', sesion: false });
        } else if (err.status === 0) {
          this.error.set({ mensaje: 'No hay conexión con el servidor.', sesion: false });
        } else {
          this.error.set({ mensaje: 'No pudimos cargar la reserva.', sesion: false });
        }
      },
    });
  }

  private aPolitica(dto: ReservaDTO): PoliticaVista | null {
    if (dto.plazoCancelacion == null || dto.porcentajeDevolucion == null) return null;
    return {
      nombre: dto.nombrePolitica ?? 'del alojamiento',
      porcentajeDevolucion: Number(dto.porcentajeDevolucion),
      plazoCancelacion: Number(dto.plazoCancelacion),
      condiciones: dto.condicionesPolitica ?? '',
    };
  }

  abrirCancelacion(): void {
    this.errorCancelacion.set('');
    this.mostrandoCancelacion.set(true);
  }

  cerrarCancelacion(): void {
    this.mostrandoCancelacion.set(false);
  }

  confirmarCancelacion(datos: CancelacionPayload): void {
    const r = this.reserva();
    if (!r || this.cancelando()) return;

    const motivo = datos.motivo.trim();
    if (!motivo || motivo.length > 500) {
      this.errorCancelacion.set('El motivo es obligatorio y debe tener máximo 500 caracteres.');
      return;
    }

    this.cancelando.set(true);
    this.errorCancelacion.set('');
    this.reservaService.cancelar(r.idReserva, motivo).subscribe({
      next: (resultado) => {
        this.cancelando.set(false);
        this.mostrandoCancelacion.set(false);
        this.reserva.update((actual) => (actual ? { ...actual, estado: 'CANCELADA' } : actual));
        const valor = Number(resultado.valorDevolucion ?? 0);
        this.mensajeConfirmacion.set(
          valor > 0
            ? `Reserva cancelada. Te devolveremos $${valor.toLocaleString('es-CO', { maximumFractionDigits: 0 })} COP a la misma tarjeta.`
            : 'Reserva cancelada. Las fechas quedaron libres; según la política no corresponde devolución.',
        );
        // La cancelación genera una notificación nueva: se actualiza el contador
        this.notificacionService.obtenerMisNotificaciones().subscribe({ error: () => {} });
      },
      error: (err: HttpErrorResponse) => {
        this.cancelando.set(false);
        if (err.status === 409 && typeof err.error === 'string' && /no está confirmada/i.test(err.error)) {
          // Ya estaba cancelada (por ejemplo, desde otra pestaña): se recarga el estado real
          this.mostrandoCancelacion.set(false);
          this.cargar();
          return;
        }
        this.errorCancelacion.set(this.textoError(err));
      },
    });
  }

  private textoError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No hay conexión con el servidor. Tu reserva sigue confirmada.';
    if (typeof err.error === 'string' && err.error.trim()) return err.error;
    return 'No se pudo cancelar la reserva. Inténtalo de nuevo.';
  }

  puedeResenar(r: ReservaVista): boolean {
    return r.estado === 'COMPLETADA' && !r.tieneResena;
  }

  abrirResena(): void {
    const r = this.reserva();
    if (!r || !this.puedeResenar(r)) return;
    this.errorResena.set('');
    this.mostrandoResena.set(true);
  }

  cerrarResena(): void {
    if (this.enviandoResena()) return;
    this.mostrandoResena.set(false);
  }

  alEnviarResena(datos: ResenaPayload): void {
    const r = this.reserva();
    if (!r || this.enviandoResena()) return;

    // Mismas reglas del back: calificación entera 1-5 y comentario de 1 a 1000 caracteres
    const comentario = datos.comentario.trim();
    if (!Number.isInteger(datos.calificacion) || datos.calificacion < 1 || datos.calificacion > 5) {
      this.errorResena.set('La calificación debe ser un número entero entre 1 y 5.');
      return;
    }
    if (!comentario || comentario.length > 1000) {
      this.errorResena.set('El comentario es obligatorio y tiene un máximo de 1000 caracteres.');
      return;
    }

    this.enviandoResena.set(true);
    this.errorResena.set('');
    this.resenaService.crear(r.idReserva, datos.calificacion, comentario).subscribe({
      next: () => {
        this.enviandoResena.set(false);
        this.mostrandoResena.set(false);
        this.reserva.update((actual) => (actual ? { ...actual, tieneResena: true } : actual));
        this.mensajeConfirmacion.set(
          'Gracias por compartir tu experiencia. Tu reseña ya aparece en el alojamiento.',
        );
      },
      error: (err: HttpErrorResponse) => {
        this.enviandoResena.set(false);
        const texto = typeof err.error === 'string' && err.error.trim() ? err.error : '';
        if (err.status === 409 && /ya tiene una reseña/i.test(texto)) {
          this.mostrandoResena.set(false);
          this.reserva.update((actual) => (actual ? { ...actual, tieneResena: true } : actual));
          this.mensajeConfirmacion.set('Esta reserva ya tenía una reseña publicada.');
          return;
        }
        if (err.status === 0) {
          this.errorResena.set('No hay conexión con el servidor. Inténtalo de nuevo.');
        } else {
          this.errorResena.set(texto || 'No se pudo publicar la reseña. Inténtalo de nuevo.');
        }
      },
    });
  }
}
