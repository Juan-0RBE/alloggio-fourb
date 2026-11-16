import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

const LONGITUD_MAX_MOTIVO = 500;

interface PoliticaVista {
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
  @Input() idReserva: number = 1042;
  @Input() titulo: string = 'Villa Moderna Vista al Valle y Montaña';
  @Input() fechaLlegada: string = '2026-12-10';
  @Input() total: number = 2835000;
  @Input() politica: PoliticaVista = {
    nombre: 'Flexible',
    porcentajeDevolucion: 20,
    plazoCancelacion: 5,
    condiciones: 'Mínimo 5 días antes de la llegada y con devolución del 20% del total.',
  };
  @Input() enviando: boolean = false;
  @Input() mensajeError: string = '';

  @Output() confirmarCancelacion = new EventEmitter<CancelacionPayload>();
  @Output() cancelar = new EventEmitter<void>();

  readonly longitudMaxMotivo = LONGITUD_MAX_MOTIVO;

  motivo: string = '';
  aceptaCondiciones: boolean = false;

  diasParaLlegada(): number {
    const llegada = new Date(this.fechaLlegada + 'T00:00:00');
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return Math.round((llegada.getTime() - hoy.getTime()) / 86400000);
  }

  puedeCancelar(): boolean {
    return this.diasParaLlegada() > 0;
  }

  aplicaDevolucion(): boolean {
    return this.diasParaLlegada() >= this.politica.plazoCancelacion;
  }

  valorDevolucion(): number {
    if (!this.aplicaDevolucion()) return 0;
    return Math.round((this.total * this.politica.porcentajeDevolucion) / 100);
  }

  motivoValido(): boolean {
    const texto = this.motivo.trim();
    return texto.length > 0 && texto.length <= LONGITUD_MAX_MOTIVO;
  }

  confirmar(): void {
    if (this.enviando || !this.motivoValido() || !this.aceptaCondiciones) return;
    this.confirmarCancelacion.emit({ motivo: this.motivo.trim() });
  }

  cerrar(): void {
    if (this.enviando) return;
    this.cancelar.emit();
  }
}
