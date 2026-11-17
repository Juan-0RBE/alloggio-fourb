import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

/** Mismo límite que valida el back (CancelacionService.LONGITUD_MAX_MOTIVO). */
const LONGITUD_MAX_MOTIVO = 500;
/** Mínimo para que el motivo sea útil; el back solo exige que no esté vacío. */
const LONGITUD_MIN_MOTIVO = 10;

export interface PoliticaVista {
  nombre: string;
  porcentajeDevolucion: number;
  plazoCancelacion: number;
  condiciones: string;
}

export interface CancelacionPayload {
  motivo: string;
}

@Component({
  selector: 'app-formulario-cancelacion',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './formulario-cancelacion.html',
  styleUrl: './formulario-cancelacion.scss',
})
export class FormularioCancelacion {
  @Input({ required: true }) idReserva!: number;
  @Input() titulo = '';
  @Input({ required: true }) fechaLlegada!: string;
  @Input() total = 0;
  /** null cuando el alojamiento no tiene política: no corresponde devolución. */
  @Input() politica: PoliticaVista | null = null;
  @Input() enviando = false;
  @Input() mensajeError = '';

  @Output() confirmarCancelacion = new EventEmitter<CancelacionPayload>();
  @Output() cancelar = new EventEmitter<void>();

  readonly longitudMaxMotivo = LONGITUD_MAX_MOTIVO;
  readonly longitudMinMotivo = LONGITUD_MIN_MOTIVO;

  motivo = '';
  aceptaCondiciones = false;
  /** El error del motivo se muestra después de que el usuario sale del campo o intenta enviar. */
  motivoTocado = false;

  /** Días completos entre hoy y la llegada, igual que ChronoUnit.DAYS.between en el back. */
  diasParaLlegada(): number {
    const [a, m, d] = this.fechaLlegada.split('-').map(Number);
    const llegada = new Date(a, m - 1, d);
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return Math.round((llegada.getTime() - hoy.getTime()) / 86400000);
  }

  puedeCancelar(): boolean {
    return this.diasParaLlegada() > 0;
  }

  aplicaDevolucion(): boolean {
    return !!this.politica && this.diasParaLlegada() >= this.politica.plazoCancelacion;
  }

  valorDevolucion(): number {
    if (!this.aplicaDevolucion() || !this.politica) return 0;
    const valor = Math.round((this.total * this.politica.porcentajeDevolucion) / 100);
    return Math.min(valor, this.total);
  }

  longitudMotivo(): number {
    return this.motivo.trim().length;
  }

  errorMotivo(): string {
    const largo = this.longitudMotivo();
    if (largo === 0) return 'Cuéntanos el motivo de la cancelación.';
    if (largo < LONGITUD_MIN_MOTIVO) {
      return `El motivo debe tener al menos ${LONGITUD_MIN_MOTIVO} caracteres.`;
    }
    if (largo > LONGITUD_MAX_MOTIVO) {
      return `El motivo no puede superar ${LONGITUD_MAX_MOTIVO} caracteres.`;
    }
    return '';
  }

  motivoValido(): boolean {
    return !this.errorMotivo();
  }

  confirmar(): void {
    this.motivoTocado = true;
    if (this.enviando || !this.puedeCancelar() || !this.motivoValido() || !this.aceptaCondiciones) {
      return;
    }
    this.confirmarCancelacion.emit({ motivo: this.motivo.trim().replace(/\s+/g, ' ') });
  }

  cerrar(): void {
    if (this.enviando) return;
    this.cancelar.emit();
  }
}
