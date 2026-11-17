import { Directive, ElementRef, HostListener, Input, OnInit, Optional, Self } from '@angular/core';
import { NgModel } from '@angular/forms';

/**
 * Tipos de filtro:
 * - enteros: solo dígitos; el modelo recibe un número (o null si queda vacío).
 * - letras: letras (con tildes y ñ), espacios, apóstrofo, punto y guion. Para nombres.
 * - ciudad: igual que letras (las ciudades no llevan números).
 * - texto: cualquier carácter imprimible; solo colapsa espacios repetidos al inicio.
 */
export type TipoFiltro = 'enteros' | 'letras' | 'ciudad' | 'texto';

const NO_LETRAS = /[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ '.\-]/g;

/**
 * Limpia lo que el usuario escribe o pega en un campo con ngModel, antes de que
 * llegue al modelo. Se usa así: <input appFiltroEntrada="enteros" [maxDigitos]="9" ...>
 */
@Directive({
  selector: 'input[appFiltroEntrada]',
  standalone: true,
})
export class FiltroEntrada implements OnInit {
  @Input('appFiltroEntrada') tipo: TipoFiltro = 'texto';
  /** Para enteros: cantidad máxima de dígitos (evita números absurdamente grandes). */
  @Input() maxDigitos = 12;

  constructor(
    private el: ElementRef<HTMLInputElement>,
    @Optional() @Self() private ngModel: NgModel | null,
  ) {}

  ngOnInit(): void {
    const input = this.el.nativeElement;
    if (this.tipo === 'enteros') {
      // type="number" acepta "e", "+", "-" y decimales; como texto se controla todo
      input.type = 'text';
      input.inputMode = 'numeric';
      input.autocomplete = 'off';
    }
  }

  @HostListener('input')
  alEscribir(): void {
    const input = this.el.nativeElement;
    const original = input.value;
    const limpio = this.limpiar(original);
    if (limpio === original && this.tipo !== 'enteros') return;

    if (limpio !== original) {
      // Conserva la posición del cursor descontando los caracteres eliminados
      const posicion = Math.max(0, (input.selectionStart ?? limpio.length) - (original.length - limpio.length));
      input.value = limpio;
      try {
        input.setSelectionRange(posicion, posicion);
      } catch {
        // Algunos tipos de input no permiten selección; no es importante
      }
    }

    const valorModelo = this.tipo === 'enteros' ? (limpio === '' ? null : Number(limpio)) : limpio;
    if (this.ngModel) {
      this.ngModel.control.setValue(valorModelo, { emitModelToViewChange: false });
      this.ngModel.viewToModelUpdate(valorModelo);
    }
  }

  private limpiar(valor: string): string {
    switch (this.tipo) {
      case 'enteros': {
        const digitos = valor.replace(/\D/g, '').slice(0, this.maxDigitos);
        // Sin ceros a la izquierda ("007" → "7"), pero se permite un "0" solo
        return digitos.replace(/^0+(?=\d)/, '');
      }
      case 'letras':
      case 'ciudad':
        return valor.replace(NO_LETRAS, '').replace(/\s{2,}/g, ' ').replace(/^\s+/, '');
      default:
        return valor.replace(/[\u0000-\u001F\u007F]/g, '').replace(/^\s+/, '').replace(/\s{3,}/g, '  ');
    }
  }
}
