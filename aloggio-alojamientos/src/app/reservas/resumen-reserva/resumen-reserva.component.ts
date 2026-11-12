import { Component, ElementRef, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

declare var Stripe: any;

const CLAVE_PUBLICABLE_STRIPE =
  'pk_test_51UXNaYBTJgL9ePBUMEkYZ3xIW5v8bBlRac3FcVIKe3B1icOJKUb2xt1XzziK9aZI7MHUbOnn2imbdePRhF4vzZ9j006XNltBGI';

@Component({
  selector: 'app-resumen-reserva',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './resumen-reserva.component.html',
  styleUrl: './resumen-reserva.component.scss',
})
export class ResumenReserva implements OnInit, OnDestroy {
  private router = inject(Router);

  cargando = false;
  nombreTitular: string = '';
  aceptaTerminos: boolean = false;
  procesandoPago = false;
  mensajeErrorTarjeta = '';
  stripeNoDisponible = false;

  cotizacion = {
    fechaLlegada: '2026-12-10',
    fechaSalida: '2026-12-15',
    noches: 5,
    precioBase: 2750000,
    limpieza: 85000,
    servicio: 0,
    total: 2835000,
  };

  alojamiento = {
    titulo: 'Villa Moderna Vista al Valle y Montaña',
    tipo: 'Villa completa',
    ciudad: 'Medellín',
    promedioCalificacion: 4.95,
    totalResenas: 42,
    imagenes: [
      {
        urlImagen:
          'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
      },
    ],
  };

  private stripe: any = null;
  private cardElement: any = null;

  @ViewChild('contenedorTarjeta')
  set contenedorTarjeta(ref: ElementRef<HTMLElement> | undefined) {
    if (ref) {
      this.montarTarjeta(ref.nativeElement);
    } else {
      this.destruirTarjeta();
    }
  }

  ngOnInit(): void {
    if (typeof Stripe === 'undefined') {
      this.stripeNoDisponible = true;
    } else {
      this.stripe = Stripe(CLAVE_PUBLICABLE_STRIPE, { locale: 'es' });
    }
  }

  ngOnDestroy(): void {
    this.destruirTarjeta();
  }

  volver(): void {
    void this.router.navigate(['/inicio']);
  }

  async confirmarYPagar(): Promise<void> {
    if (!this.aceptaTerminos || this.procesandoPago) return;

    if (!this.stripe || !this.cardElement) {
      this.mensajeErrorTarjeta = 'El formulario de pago no está disponible. Recarga la página.';
      return;
    }
    if (!this.nombreTitular.trim()) {
      this.mensajeErrorTarjeta = 'Ingresa el nombre del titular de la tarjeta.';
      return;
    }

    this.procesandoPago = true;
    this.mensajeErrorTarjeta = '';

    try {
      const { error } = await this.stripe.createPaymentMethod({
        type: 'card',
        card: this.cardElement,
        billing_details: { name: this.nombreTitular.trim() },
      });

      if (error) {
        this.mensajeErrorTarjeta = error.message;
        this.procesandoPago = false;
        return;
      }

      setTimeout(() => {
        this.procesandoPago = false;
        alert('¡Pago procesado con éxito mediante Stripe!');
        void this.router.navigate(['/inicio']);
      }, 1500);
    } catch (e) {
      this.mensajeErrorTarjeta = 'Ocurrió un error al procesar el pago.';
      this.procesandoPago = false;
    }
  }

  private montarTarjeta(elemento: HTMLElement): void {
    if (!this.stripe || this.cardElement) return;

    const elementos = this.stripe.elements();

    this.cardElement = elementos.create('card', {
      style: {
        base: {
          color: '#ffffff',
          fontFamily: 'sans-serif',
          fontSize: '16px',
          '::placeholder': { color: '#9ca3af' },
        },
        invalid: { color: '#ff4d4d' },
      },
    });

    this.cardElement.mount(elemento);
    this.cardElement.on('change', (evento: any) => {
      this.mensajeErrorTarjeta = evento.error ? evento.error.message : '';
    });
  }

  private destruirTarjeta(): void {
    this.cardElement?.destroy();
    this.cardElement = null;
  }
}
