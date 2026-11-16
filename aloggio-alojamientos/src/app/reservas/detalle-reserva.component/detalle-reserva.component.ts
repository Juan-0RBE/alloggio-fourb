import { Component, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { ETIQUETAS_ESTADO, ReservaVista } from '../mis-reservas/mis-reservas.component';
import {
  CancelacionPayload,
  FormularioCancelacion,
} from '../formulario-cancelacion/formulario-cancelacion.component';
import { FormularioResena } from '../../resenas/formulario-resena/formulario-resena.component';

const DESCRIPCIONES_ESTADO: Record<ReservaVista['estado'], string> = {
  CONFIRMADA: 'Tu reserva está confirmada y las fechas están bloqueadas para ti.',
  COMPLETADA: 'Tu estadía terminó. Ya puedes dejar una reseña del alojamiento.',
  CANCELADA: 'Esta reserva fue cancelada y las fechas quedaron libres.',
};

@Component({
  selector: 'app-detalle-reserva',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, FormularioCancelacion, FormularioResena],
  templateUrl: './detalle-reserva.component.html',
  styleUrl: './detalle-reserva.component.scss',
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

  reserva = signal<ReservaVista | null>({
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
  });

  politica = {
    nombre: 'Flexible',
    porcentajeDevolucion: 20,
    plazoCancelacion: 5,
    condiciones: 'Mínimo 5 días antes de la llegada y con devolución del 20% del total.',
  };

  constructor(private ruta: ActivatedRoute) {}

  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    if (!id) {
      this.error.set({ mensaje: 'No encontramos esta reserva.', sesion: false });
      return;
    }
    this.reserva.update((r) => (r ? { ...r, idReserva: id } : r));
  }

  cargar(): void {
    this.error.set(null);
    this.cargando.set(true);
    setTimeout(() => {
      this.cargando.set(false);
    }, 400);
  }

  abrirCancelacion(): void {
    this.errorCancelacion.set('');
    this.mostrandoCancelacion.set(true);
  }

  cerrarCancelacion(): void {
    this.mostrandoCancelacion.set(false);
  }

  confirmarCancelacion(_datos: CancelacionPayload): void {
    this.cancelando.set(true);
    setTimeout(() => {
      this.reserva.update((r) => (r ? { ...r, estado: 'CANCELADA' } : r));
      this.cancelando.set(false);
      this.mostrandoCancelacion.set(false);
      this.mensajeConfirmacion.set('Reserva cancelada. Las fechas quedaron libres.');
    }, 400);
  }

  abrirResena(): void {
    this.mostrandoResena.set(true);
  }

  cerrarResena(): void {
    this.mostrandoResena.set(false);
  }

  alEnviarResena(): void {
    this.mostrandoResena.set(false);
    this.mensajeConfirmacion.set('Gracias por compartir tu experiencia.');
  }
}
