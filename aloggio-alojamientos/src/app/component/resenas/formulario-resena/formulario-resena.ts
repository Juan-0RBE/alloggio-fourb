import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Estrella } from '../estrella/estrella';

interface ReseñaPayload {
  calificacionGeneral: number;
  limpieza: number;
  comunicacion: number;
  exactitud: number;
  comentario: string;
}

@Component({
  selector: 'app-formulario-resena',
  standalone: true,
  imports: [CommonModule, FormsModule, Estrella],
  templateUrl: './formulario-resena.html',
  styleUrls: ['./formulario-resena.scss'],
})
export class FormularioResena {
  @Input() alojamientoTitulo: string = 'Villa Moderna Vista al Valle';
  @Output() enviarReseña = new EventEmitter<ReseñaPayload>();
  @Output() cancelar = new EventEmitter<void>();

  calificacionGeneral: number = 5;
  limpieza: number = 5;
  comunicacion: number = 5;
  exactitud: number = 5;
  comentario: string = '';

  enviar(): void {
    if (!this.comentario.trim()) return;

    const nuevaReseña: ReseñaPayload = {
      calificacionGeneral: this.calificacionGeneral,
      limpieza: this.limpieza,
      comunicacion: this.comunicacion,
      exactitud: this.exactitud,
      comentario: this.comentario,
    };

    this.enviarReseña.emit(nuevaReseña);
  }

  cancelarFormulario(): void {
    this.cancelar.emit();
  }
}
