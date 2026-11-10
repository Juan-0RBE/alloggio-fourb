import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-estrella',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="contenedor-estrellas" [class.interactivo]="interactivo">
      <button
        type="button"
        *ngFor="let estrella of [1, 2, 3, 4, 5]; let i = index"
        class="boton-estrella"
        [class.activa]="estrella <= (hoverCalificacion || calificacion)"
        (mouseenter)="onMouseEnter(estrella)"
        (mouseleave)="onMouseLeave()"
        (click)="seleccionarCalificacion(estrella)"
        [disabled]="!interactivo"
      >
        <i
          [class]="
            estrella <= (hoverCalificacion || calificacion) ? 'pi pi-star-fill' : 'pi pi-star'
          "
        ></i>
      </button>
      <span class="texto-valor" *ngIf="mostrarValor && !interactivo">{{
        calificacion | number: '1.1-1'
      }}</span>
    </div>
  `,
  styles: [
    `
      .contenedor-estrellas {
        display: flex;
        align-items: center;
        gap: 0.25rem;

        .boton-estrella {
          background: transparent;
          border: none;
          padding: 0;
          cursor: default;
          font-size: 0.95rem;
          color: #4b5563;
          transition:
            color 0.15s ease,
            transform 0.15s ease;

          i {
            color: inherit;
          }

          &.activa {
            color: #e5b869;
          }
        }

        &.interactivo {
          .boton-estrella {
            cursor: pointer;

            &:hover {
              transform: scale(1.15);
            }
          }
        }

        .texto-valor {
          font-size: 0.85rem;
          font-weight: 600;
          color: #f4f5f7;
          margin-left: 0.3rem;
        }
      }
    `,
  ],
})
export class Estrella {
  @Input() calificacion: number = 5;
  @Input() interactivo: boolean = false;
  @Input() mostrarValor: boolean = false;

  @Output() calificacionCambiada = new EventEmitter<number>();

  hoverCalificacion: number = 0;

  onMouseEnter(estrella: number) {
    if (this.interactivo) {
      this.hoverCalificacion = estrella;
    }
  }

  onMouseLeave() {
    if (this.interactivo) {
      this.hoverCalificacion = 0;
    }
  }

  seleccionarCalificacion(estrella: number) {
    if (this.interactivo) {
      this.calificacion = estrella;
      this.calificacionCambiada.emit(estrella);
    }
  }
}
