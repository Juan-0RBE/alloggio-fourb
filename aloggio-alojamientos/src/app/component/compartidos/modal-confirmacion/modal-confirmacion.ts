import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal-confirmacion',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal-confirmacion.html',
  styleUrl: './modal-confirmacion.scss',
})
export class ModalConfirmacion {
  visible = input<boolean>(false);
  titulo = input<string>('¿Estás seguro?');
  mensaje = input<string>('');
  icono = input<string>('pi-exclamation-triangle');
  tipo = input<'normal' | 'peligro'>('normal');
  textoConfirmar = input<string>('Confirmar');
  textoCancelar = input<string>('Cancelar');
  mostrarCancelar = input<boolean>(true);
  confirmarDeshabilitado = input<boolean>(false);
  cargando = input<boolean>(false);

  idBase = 'modal-' + Math.random().toString(36).substring(2, 9);

  confirmar = output<void>();
  cancelar = output<void>();

  alClicFondo(): void {
    this.cancelar.emit();
  }
}
