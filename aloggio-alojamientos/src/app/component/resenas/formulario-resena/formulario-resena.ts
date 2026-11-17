import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Estrella } from '../estrella/estrella';

/** Mismos límites que valida el back (ResenaService). */
const CALIFICACION_MIN = 1;
const CALIFICACION_MAX = 5;
const COMENTARIO_MAX = 1000;
/** Mínimo para que la reseña aporte algo; el back solo exige que no esté vacía. */
const COMENTARIO_MIN = 10;

export interface ResenaPayload {
  calificacion: number;
  comentario: string;
}

const TEXTOS_CALIFICACION: Record<number, string> = {
  1: 'Muy mala',
  2: 'Mala',
  3: 'Regular',
  4: 'Buena',
  5: 'Excelente',
};

@Component({
  selector: 'app-formulario-resena',
  standalone: true,
  imports: [CommonModule, FormsModule, Estrella],
  templateUrl: './formulario-resena.html',
  styleUrls: ['./formulario-resena.scss'],
})
export class FormularioResena {
  @Input() alojamientoTitulo = '';
  @Input() enviando = false;
  @Input() mensajeError = '';

  @Output() enviarReseña = new EventEmitter<ResenaPayload>();
  @Output() cancelar = new EventEmitter<void>();

  readonly comentarioMax = COMENTARIO_MAX;
  readonly comentarioMin = COMENTARIO_MIN;

  /** 0 = sin elegir: el huésped debe escoger la calificación explícitamente. */
  calificacion = 0;
  comentario = '';
  intentoEnviar = false;
  comentarioTocado = false;

  textoCalificacion(): string {
    return TEXTOS_CALIFICACION[this.calificacion] ?? 'Elige de 1 a 5 estrellas';
  }

  elegirCalificacion(valor: number): void {
    if (this.enviando) return;
    this.calificacion = valor;
  }

  longitudComentario(): number {
    return this.comentario.trim().length;
  }

  errorCalificacion(): string {
    const valida =
      Number.isInteger(this.calificacion) &&
      this.calificacion >= CALIFICACION_MIN &&
      this.calificacion <= CALIFICACION_MAX;
    return valida ? '' : 'Elige una calificación de 1 a 5 estrellas.';
  }

  errorComentario(): string {
    const largo = this.longitudComentario();
    if (largo === 0) return 'Escribe un comentario sobre tu estadía.';
    if (largo < COMENTARIO_MIN) return `El comentario debe tener al menos ${COMENTARIO_MIN} caracteres.`;
    if (largo > COMENTARIO_MAX) return `El comentario no puede superar ${COMENTARIO_MAX} caracteres.`;
    return '';
  }

  esValido(): boolean {
    return !this.errorCalificacion() && !this.errorComentario();
  }

  enviar(): void {
    this.intentoEnviar = true;
    this.comentarioTocado = true;
    if (this.enviando || !this.esValido()) return;

    this.enviarReseña.emit({
      calificacion: this.calificacion,
      comentario: this.comentario.trim().replace(/[ \t]+/g, ' '),
    });
  }

  cancelarFormulario(): void {
    if (this.enviando) return;
    this.cancelar.emit();
  }
}
