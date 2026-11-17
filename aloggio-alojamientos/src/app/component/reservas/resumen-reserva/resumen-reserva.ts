import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, of, switchMap } from 'rxjs';

import { AlojamientoService } from '../../../service/alojamiento.service';
import { ReservaService } from '../../../service/reserva.service';
import { AlojamientoDetalleDTO } from '../../../model/alojamiento.model';
import { CotizacionDTO } from '../../../model/reserva.model';
import { FiltroEntrada } from '../../../directive/filtro-entrada.directive';

declare var Stripe: any;

const CLAVE_PUBLICABLE_STRIPE =
  'pk_test_51UXNaYBTJgL9ePBUMEkYZ3xIW5v8bBlRac3FcVIKe3B1icOJKUb2xt1XzziK9aZI7MHUbOnn2imbdePRhF4vzZ9j006XNltBGI';

/** Mismo patrón que valida el back (ReservaService.PATRON_METODO_PAGO). */
const PATRON_METODO_PAGO = /^pm_[A-Za-z0-9_]{3,100}$/;
const PATRON_FECHA = /^\d{4}-\d{2}-\d{2}$/;
/** Nombre y apellido: solo letras (con tildes), separados por espacio, apóstrofo, punto o guion. */
const PATRON_TITULAR = /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?:[ '.-]+[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)+\.?$/;
const LONGITUD_MIN_TITULAR = 5;
const LONGITUD_MAX_TITULAR = 60;

/** Datos de la reserva que llegan en la URL desde el detalle del alojamiento. */
interface SolicitudReserva {
  idPublicacion: number;
  fechaLlegada: string;
  fechaSalida: string;
  huespedes: number;
  idsServicios: number[];
}

type TipoAviso = 'rechazo' | 'pasarela' | 'vencida' | 'general';

interface AvisoPago {
  tipo: TipoAviso;
  texto: string;
}

@Component({
  selector: 'app-resumen-reserva',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, FiltroEntrada],
  templateUrl: './resumen-reserva.html',
  styleUrl: './resumen-reserva.scss',
})
export class ResumenReserva implements OnInit, OnDestroy {
  private router = inject(Router);
  private ruta = inject(ActivatedRoute);
  private alojamientoService = inject(AlojamientoService);
  private reservaService = inject(ReservaService);

  // La app es zoneless: todo lo que cambia tras una petición o un evento de Stripe es un signal

  cargando = signal(true);
  errorCarga = signal<string | null>(null);

  solicitud = signal<SolicitudReserva | null>(null);
  alojamiento = signal<AlojamientoDetalleDTO | null>(null);
  cotizacion = signal<CotizacionDTO | null>(null);

  stripeNoDisponible = signal(false);
  tarjetaCompleta = signal(false);
  errorTarjeta = signal('');
  errorTitular = signal('');
  errorTerminos = signal('');
  avisoPago = signal<AvisoPago | null>(null);
  procesandoPago = signal(false);
  recotizando = signal(false);

  /** Segundos que le quedan a la cotización. */
  segundosRestantes = signal(0);

  nombreTitular = '';
  aceptaTerminos = false;

  cotizacionVencida = computed(() => this.cotizacion() !== null && this.segundosRestantes() <= 0);

  tiempoRestante = computed(() => {
    const total = Math.max(0, this.segundosRestantes());
    const min = Math.floor(total / 60);
    const seg = String(total % 60).padStart(2, '0');
    return `${min}:${seg}`;
  });

  puedePagar = computed(
    () =>
      !!this.cotizacion() &&
      !this.cotizacionVencida() &&
      !this.procesandoPago() &&
      !this.stripeNoDisponible() &&
      (this.cotizacion()?.total ?? 0) > 0,
  );

  private stripe: any = null;
  private cardElement: any = null;
  private temporizador: ReturnType<typeof setInterval> | null = null;

  /**
   * Clave de idempotencia del intento de pago actual. Se conserva si el pago falla
   * por la pasarela o la red (reintentar no cobra dos veces) y se renueva cuando
   * cambia la tarjeta, la cotización o el pago es rechazado.
   */
  private claveIdempotencia = '';
  /** PaymentMethod ya creado para la clave actual; se reutiliza en los reintentos. */
  private metodoPagoActual: string | null = null;

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
      this.stripeNoDisponible.set(true);
    } else {
      this.stripe = Stripe(CLAVE_PUBLICABLE_STRIPE, { locale: 'es' });
    }

    const resultado = this.leerSolicitud();
    if (typeof resultado === 'string') {
      this.cargando.set(false);
      this.errorCarga.set(resultado);
      return;
    }
    this.solicitud.set(resultado);
    this.cargarAlojamientoYCotizar(resultado);
  }

  ngOnDestroy(): void {
    this.destruirTarjeta();
    this.detenerTemporizador();
  }

  // Navegación

  volver(): void {
    const sol = this.solicitud();
    if (sol) {
      void this.router.navigate(['/detalle-alojamiento', sol.idPublicacion]);
    } else {
      void this.router.navigate(['/inicio']);
    }
  }

  // Carga inicial

  /** Valida los parámetros de la URL. Devuelve la solicitud o el mensaje de error. */
  private leerSolicitud(): SolicitudReserva | string {
    const q = this.ruta.snapshot.queryParamMap;
    const invalida = 'Los datos de la reserva no son válidos. Vuelve al alojamiento y elige las fechas de nuevo.';

    const idTexto = q.get('id') ?? '';
    if (!/^\d+$/.test(idTexto) || Number(idTexto) <= 0) return invalida;

    const llegada = q.get('llegada') ?? '';
    const salida = q.get('salida') ?? '';
    if (!this.esFechaValida(llegada) || !this.esFechaValida(salida)) return invalida;
    if (llegada < this.hoyIso()) return 'La fecha de llegada no puede ser anterior a hoy.';
    if (salida <= llegada) return 'La fecha de salida debe ser posterior a la de llegada.';

    const huespedesTexto = q.get('huespedes') ?? '';
    if (!/^\d+$/.test(huespedesTexto) || Number(huespedesTexto) < 1) return invalida;

    const serviciosTexto = q.get('servicios') ?? '';
    let idsServicios: number[] = [];
    if (serviciosTexto) {
      const partes = serviciosTexto.split(',');
      if (partes.some((p) => !/^\d+$/.test(p) || Number(p) <= 0)) return invalida;
      idsServicios = [...new Set(partes.map(Number))];
    }

    return {
      idPublicacion: Number(idTexto),
      fechaLlegada: llegada,
      fechaSalida: salida,
      huespedes: Number(huespedesTexto),
      idsServicios,
    };
  }

  /** Trae el alojamiento (para validar capacidad y servicios) y pide la cotización real. */
  private cargarAlojamientoYCotizar(sol: SolicitudReserva): void {
    this.cargando.set(true);
    this.errorCarga.set(null);

    this.alojamientoService.obtenerDetalle(sol.idPublicacion).subscribe({
      next: (detalle) => {
        const mensaje = this.validarContraAlojamiento(sol, detalle);
        if (mensaje) {
          this.cargando.set(false);
          this.errorCarga.set(mensaje);
          return;
        }
        this.alojamiento.set(detalle);
        this.pedirCotizacion(sol, () => this.cargando.set(false));
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.errorCarga.set(
          err.status === 404
            ? 'El alojamiento no existe o ya no está disponible.'
            : this.textoError(err, 'No se pudo cargar el alojamiento.'),
        );
      },
    });
  }

  private validarContraAlojamiento(
    sol: SolicitudReserva,
    detalle: AlojamientoDetalleDTO,
  ): string | null {
    if (sol.huespedes > detalle.capacidad) {
      return `Este alojamiento admite máximo ${detalle.capacidad} huéspedes.`;
    }
    const idsAdicionales = new Set(
      detalle.servicios.filter((s) => s.esAdicional).map((s) => s.idServicio),
    );
    if (sol.idsServicios.some((id) => !idsAdicionales.has(id))) {
      return 'Alguno de los servicios elegidos no pertenece a este alojamiento.';
    }
    const cruza = detalle.calendarioOcupado.some(
      (r) => sol.fechaLlegada < r.fechaFin && sol.fechaSalida > r.fechaInicio,
    );
    if (cruza) {
      return 'El alojamiento no está disponible en las fechas elegidas.';
    }
    return null;
  }

  /** Crea la cotización en el back y le agrega los servicios adicionales elegidos. */
  private pedirCotizacion(sol: SolicitudReserva, alTerminar: () => void): void {
    this.reservaService
      .cotizar(sol.idPublicacion, sol.fechaLlegada, sol.fechaSalida)
      .pipe(
        switchMap((cot): Observable<CotizacionDTO> =>
          sol.idsServicios.length && cot.idCotizacion
            ? this.reservaService.agregarServicios(cot.idCotizacion, sol.idsServicios)
            : of(cot),
        ),
      )
      .subscribe({
        next: (cot) => {
          if (!cot.idCotizacion || !(Number(cot.total) > 0)) {
            alTerminar();
            this.errorCarga.set('La cotización recibida no es válida. Intenta de nuevo.');
            return;
          }
          this.cotizacion.set(cot);
          this.nuevoIntentoDePago();
          this.avisoPago.set(null);
          this.iniciarTemporizador(cot.vigencia);
          alTerminar();
        },
        error: (err: HttpErrorResponse) => {
          alTerminar();
          this.errorCarga.set(this.textoError(err, 'No se pudo calcular la cotización.'));
        },
      });
  }

  /** Pide una cotización nueva cuando la anterior venció. */
  recotizar(): void {
    const sol = this.solicitud();
    if (!sol || this.recotizando()) return;
    this.recotizando.set(true);
    this.pedirCotizacion(sol, () => this.recotizando.set(false));
  }

  // Validación del formulario

  validarTitular(): boolean {
    const nombre = this.nombreTitular.trim().replace(/\s+/g, ' ');
    let mensaje = '';
    if (!nombre) {
      mensaje = 'Ingresa el nombre del titular de la tarjeta.';
    } else if (nombre.length < LONGITUD_MIN_TITULAR || nombre.length > LONGITUD_MAX_TITULAR) {
      mensaje = `El nombre debe tener entre ${LONGITUD_MIN_TITULAR} y ${LONGITUD_MAX_TITULAR} caracteres.`;
    } else if (!PATRON_TITULAR.test(nombre)) {
      mensaje = 'Escribe nombre y apellido tal como aparecen en la tarjeta, solo con letras.';
    }
    this.errorTitular.set(mensaje);
    return !mensaje;
  }

  private validarFormulario(): boolean {
    const titularOk = this.validarTitular();

    let tarjetaOk = true;
    if (!this.stripe || !this.cardElement) {
      this.errorTarjeta.set('El formulario de pago no está disponible. Recarga la página.');
      tarjetaOk = false;
    } else if (!this.tarjetaCompleta()) {
      if (!this.errorTarjeta()) {
        this.errorTarjeta.set('Completa el número, la fecha de vencimiento y el CVC de la tarjeta.');
      }
      tarjetaOk = false;
    }

    const terminosOk = this.aceptaTerminos;
    this.errorTerminos.set(terminosOk ? '' : 'Debes aceptar las reglas y los términos para continuar.');

    return titularOk && tarjetaOk && terminosOk;
  }

  // Pago

  async confirmarYPagar(): Promise<void> {
    if (!this.puedePagar()) return;
    this.avisoPago.set(null);

    const cot = this.cotizacion();
    if (!cot?.idCotizacion) return;
    if (this.cotizacionVencida()) {
      this.avisoPago.set({ tipo: 'vencida', texto: 'La cotización venció. Actualízala para pagar.' });
      return;
    }
    if (!this.validarFormulario()) return;

    this.procesandoPago.set(true);

    // 1. Stripe convierte la tarjeta en un PaymentMethod; el número nunca llega a nuestro back
    let metodoPago = this.metodoPagoActual;
    if (!metodoPago) {
      try {
        const { paymentMethod, error } = await this.stripe.createPaymentMethod({
          type: 'card',
          card: this.cardElement,
          billing_details: { name: this.nombreTitular.trim().replace(/\s+/g, ' ') },
        });
        if (error) {
          this.errorTarjeta.set(error.message ?? 'Revisa los datos de la tarjeta.');
          this.procesandoPago.set(false);
          return;
        }
        metodoPago = paymentMethod?.id ?? null;
      } catch {
        this.avisoPago.set({
          tipo: 'pasarela',
          texto: 'No pudimos comunicarnos con Stripe. Revisa tu conexión e inténtalo de nuevo.',
        });
        this.procesandoPago.set(false);
        return;
      }
    }

    if (!metodoPago || !PATRON_METODO_PAGO.test(metodoPago)) {
      this.errorTarjeta.set('La tarjeta no pudo validarse. Intenta de nuevo.');
      this.procesandoPago.set(false);
      return;
    }
    this.metodoPagoActual = metodoPago;

    // 2. El back cobra la cotización y crea la reserva
    this.reservaService.crearReserva(cot.idCotizacion, metodoPago, this.claveIdempotencia).subscribe({
      next: (reserva) => {
        this.procesandoPago.set(false);
        this.detenerTemporizador();
        void this.router.navigate(['/resultado-reserva'], {
          queryParams: { tipo: 'exito', id: reserva.idReserva },
          replaceUrl: true,
        });
      },
      error: (err: HttpErrorResponse) => {
        this.procesandoPago.set(false);
        this.manejarErrorReserva(err);
      },
    });
  }

  private manejarErrorReserva(err: HttpErrorResponse): void {
    const texto = this.textoError(err, 'No se pudo completar la reserva.');

    switch (err.status) {
      case 402:
        // Rechazo definitivo: el próximo intento (otra tarjeta) usa una clave nueva
        this.nuevoIntentoDePago();
        this.avisoPago.set({
          tipo: 'rechazo',
          texto: `${texto}. No se hizo ningún cobro; puedes intentar con otra tarjeta.`,
        });
        return;
      case 410:
        this.segundosRestantes.set(0);
        this.avisoPago.set({ tipo: 'vencida', texto });
        return;
      case 409:
        if (/disponible|no puede recibir/i.test(texto)) {
          void this.router.navigate(['/resultado-reserva'], {
            queryParams: { tipo: 'no-disponible', id: this.solicitud()?.idPublicacion },
          });
        } else {
          void this.router.navigate(['/resultado-reserva'], {
            queryParams: { tipo: 'conflicto', mensaje: texto },
          });
        }
        return;
      case 0:
      case 502:
        // No se sabe si hubo cobro: se conserva la clave para que el reintento no cobre dos veces
        this.avisoPago.set({
          tipo: 'pasarela',
          texto:
            err.status === 0
              ? 'Se perdió la conexión con el servidor. Puedes reintentar sin riesgo de un doble cobro.'
              : `${texto}. Puedes reintentar sin riesgo de un doble cobro.`,
        });
        return;
      default:
        this.nuevoIntentoDePago();
        this.avisoPago.set({ tipo: 'general', texto });
    }
  }

  /** Empieza un intento de pago nuevo: nueva clave de idempotencia y nuevo PaymentMethod. */
  private nuevoIntentoDePago(): void {
    this.claveIdempotencia = this.generarClave();
    this.metodoPagoActual = null;
  }

  private generarClave(): string {
    if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
      return crypto.randomUUID();
    }
    return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 12)}`;
  }

  // Stripe Elements

  private montarTarjeta(elemento: HTMLElement): void {
    if (!this.stripe || this.cardElement) return;

    const elementos = this.stripe.elements({ locale: 'es' });

    this.cardElement = elementos.create('card', {
      hidePostalCode: true,
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
      this.tarjetaCompleta.set(!!evento.complete && !evento.error);
      this.errorTarjeta.set(evento.error ? evento.error.message : '');
      // La tarjeta cambió: el PaymentMethod anterior ya no sirve
      if (this.metodoPagoActual) this.nuevoIntentoDePago();
    });
  }

  private destruirTarjeta(): void {
    this.cardElement?.destroy();
    this.cardElement = null;
    this.tarjetaCompleta.set(false);
  }

  // Vigencia de la cotización

  private iniciarTemporizador(vigencia: string | undefined): void {
    this.detenerTemporizador();
    // La vigencia llega como fecha-hora local del servidor, sin zona
    const limite = vigencia ? new Date(vigencia).getTime() : NaN;
    if (isNaN(limite)) {
      this.segundosRestantes.set(30 * 60);
    }
    const fin = isNaN(limite) ? Date.now() + 30 * 60 * 1000 : limite;

    const actualizar = () => {
      const restantes = Math.floor((fin - Date.now()) / 1000);
      this.segundosRestantes.set(Math.max(0, restantes));
      if (restantes <= 0) this.detenerTemporizador();
    };
    actualizar();
    this.temporizador = setInterval(actualizar, 1000);
  }

  private detenerTemporizador(): void {
    if (this.temporizador) clearInterval(this.temporizador);
    this.temporizador = null;
  }

  // Utilidades

  private esFechaValida(texto: string): boolean {
    if (!PATRON_FECHA.test(texto)) return false;
    const [a, m, d] = texto.split('-').map(Number);
    const fecha = new Date(a, m - 1, d);
    return fecha.getFullYear() === a && fecha.getMonth() === m - 1 && fecha.getDate() === d;
  }

  private hoyIso(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  /** Convierte yyyy-MM-dd a Date local (evita el desfase de zona horaria). */
  aFecha(iso: string | undefined): Date | null {
    if (!iso) return null;
    const [a, m, d] = iso.split('-').map(Number);
    return new Date(a, m - 1, d);
  }

  private textoError(err: unknown, porDefecto: string): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 0) return 'No hay conexión con el servidor.';
      if (typeof err.error === 'string' && err.error.trim()) return err.error;
      if (err.error?.message) return err.error.message;
    }
    return porDefecto;
  }
}
