import {
  Component,
  OnInit,
  AfterViewInit,
  OnDestroy,
  ChangeDetectorRef,
  inject,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

declare var Stripe: any;

interface ResumenReservaData {
  alojamientoId: number;
  titulo: string;
  tipo: string;
  ubicacion: string;
  imagenUrl: string;
  calificacion: number;
  resenasCount: number;
  anfitrionNombre: string;
  precioNoche: number;
  fechaLlegada: string;
  fechaSalida: string;
  numeroNoches: number;
  cantidadHuespedes: number;
  tarifaLimpieza: number;
  subtotalHospedaje: number;
  totalReserva: number;
}

@Component({
  imports: [CommonModule, FormsModule],
  selector: 'app-resumen-reserva.component',
  styleUrl: './resumen-reserva.component.scss',
  templateUrl: './resumen-reserva.component.html',
})
export class ResumenReserva implements OnInit, AfterViewInit, OnDestroy {
  datosReserva: ResumenReservaData = {
    alojamientoId: 1,
    titulo: '',
    tipo: '',
    ubicacion: '',
    imagenUrl: '',
    calificacion: 1,
    resenasCount: 1,
    anfitrionNombre: '',
    precioNoche: 1,
    fechaLlegada: '',
    fechaSalida: '',
    numeroNoches: 1,
    cantidadHuespedes: 1,
    tarifaLimpieza: 1,
    subtotalHospedaje: 1,
    totalReserva: 1,
  };

  nombreTitular: string = '';
  aceptaTerminos: boolean = false;
  procesandoPago: boolean = false;
  mensajeErrorTarjeta: string = '';

  private cdr = inject(ChangeDetectorRef);
  private stripe: any;
  private cardElement: any;

  ngOnInit(): void {
    this.stripe = Stripe(
      'pk_test_51UXNaYBTJgL9ePBUMEkYZ3xIW5v8bBlRac3FcVIKe3B1icOJKUb2xt1XzziK9aZI7MHUbOnn2imbdePRhF4vzZ9j006XNltBGI',
      { locale: 'es' },
    );
  }

  ngAfterViewInit(): void {
    this.inicializarStripeElements();
  }

  ngOnDestroy(): void {
    this.cardElement?.destroy();
  }

  private inicializarStripeElements(): void {
    const elementos = this.stripe.elements({
      fonts: [
        {
          cssSrc:
            'https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600&display=swap',
        },
      ],
    });

    this.cardElement = elementos.create('card', {
      style: {
        base: {
          color: '#f4f5f7',
          fontFamily: '"Plus Jakarta Sans", sans-serif',
          fontSize: '16px',
          '::placeholder': { color: '#9ba3af' },
        },
        invalid: { color: '#ff4d4d' },
      },
    });

    this.cardElement.mount('#stripe-card-element');

    this.cardElement.on('change', (evento: any) => {
      this.mensajeErrorTarjeta = evento.error ? evento.error.message : '';
      this.cdr.markForCheck();
    });
  }

  async confirmarYPagar(): Promise<void> {
    if (!this.aceptaTerminos || this.procesandoPago) return;

    if (!this.nombreTitular.trim()) {
      this.mensajeErrorTarjeta = 'Ingresa el nombre del titular de la tarjeta.';
      return;
    }

    this.procesandoPago = true;
    this.mensajeErrorTarjeta = '';

    try {
      const { paymentMethod, error } = await this.stripe.createPaymentMethod({
        type: 'card',
        card: this.cardElement,
        billing_details: { name: this.nombreTitular.trim() },
      });

      if (error) {
        this.mensajeErrorTarjeta = error.message;
        return;
      }

      await new Promise((resolve) => setTimeout(resolve, 1500));
      console.log('PaymentMethod de prueba:', paymentMethod.id);
      alert('¡Reserva y pago con Stripe procesados con éxito!');
    } catch (error) {
      console.error('Error procesando el pago', error);
      this.mensajeErrorTarjeta = 'No se pudo procesar el pago. Inténtalo de nuevo.';
    } finally {
      this.procesandoPago = false;
      this.cdr.markForCheck();
    }
  }
}
