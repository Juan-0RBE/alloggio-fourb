import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AlojamientoService } from '../../../service/alojamiento.service';
import { AuthService } from '../../../service/auth.service';
import { AlojamientoDetalleDTO } from '../../../model/alojamiento.model';
import { ServicioAlojamientoDTO, TipoAlojamiento } from '../../../model/publicacion.model';
import { FiltroEntrada } from '../../../directive/filtro-entrada.directive';

type EstadoDia = 'LIBRE' | 'OCUPADO' | 'PASADO';

interface DiaCalendario {
  fecha: string;
  numero: number;
  estado: EstadoDia;
}

export const ETIQUETAS_TIPO: Record<TipoAlojamiento, string> = {
  CASA: 'Casa',
  APARTAMENTO: 'Apartamento',
  HABITACION: 'Habitación',
  CABAÑA: 'Cabaña',
};

// Porcentajes de ejemplo: la plataforma los define y /cotizacion/crear devuelve los valores reales.
const PORCENTAJE_SERVICIO = 10;
const PORCENTAJE_DESCUENTO = 10;
const NOCHES_MINIMAS_DESCUENTO = 7;

@Component({
  selector: 'app-detalle-alojamiento',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, FiltroEntrada],
  templateUrl: './detalle-alojamiento.html',
  styleUrl: './detalle-alojamiento.scss',
})
export class DetalleAlojamiento implements OnInit {
  readonly etiquetasTipo = ETIQUETAS_TIPO;
  readonly estrellas = [1, 2, 3, 4, 5];
  readonly diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  private ruta = inject(ActivatedRoute);
  private router = inject(Router);
  private alojamientoService = inject(AlojamientoService);
  auth = inject(AuthService);

  // La app es zoneless: el estado que cambia tras una petición HTTP debe ser un signal
  // para que la vista se vuelva a pintar. Los getters mantienen los nombres del template.
  private readonly detalle = signal<AlojamientoDetalleDTO | null>(null);
  private readonly cargandoSig = signal(false);
  private readonly errorSig = signal<string | null>(null);

  get alojamiento(): AlojamientoDetalleDTO | null {
    return this.detalle();
  }

  get cargando(): boolean {
    return this.cargandoSig();
  }

  get error(): string | null {
    return this.errorSig();
  }

  serviciosIncluidos: ServicioAlojamientoDTO[] = [];
  serviciosAdicionales: ServicioAlojamientoDTO[] = [];

  private _fechaLlegada: string = '';

  get fechaLlegada(): string {
    return this._fechaLlegada;
  }

  set fechaLlegada(val: string) {
    this._fechaLlegada = val;

    // Validar que fechaSalida sea estrictamente mayor a la nueva llegada (mínimo 1 día después)
    if (this.fechaSalida && this.fechaSalida <= val) {
      this.fechaSalida = '';
    }
  }

  fechaSalida: string = '';
  cantidadHuespedes: number = 2;
  idsServiciosSeleccionados: number[] = [];

  indiceImagenSeleccionada: number | null = null;

  mesVisible: Date = new Date(new Date().getFullYear(), new Date().getMonth(), 1);
  celdasCalendario: (DiaCalendario | null)[] = [];

  ngOnInit(): void {
    // Se escucha el parámetro para recargar si se navega de un detalle a otro
    this.ruta.paramMap.subscribe((params) => {
      const id = Number(params.get('id'));
      if (!id) {
        this.detalle.set(null);
        this.errorSig.set('No encontramos este alojamiento.');
        return;
      }
      this.cargar(id);
    });
  }

  cargar(id: number): void {
    this.cargandoSig.set(true);
    this.errorSig.set(null);
    this.alojamientoService.obtenerDetalle(id).subscribe({
      next: (detalle) => {
        this.serviciosIncluidos = detalle.servicios.filter((s) => !s.esAdicional);
        this.serviciosAdicionales = detalle.servicios.filter((s) => s.esAdicional);
        this.idsServiciosSeleccionados = [];
        this.indiceImagenSeleccionada = null;
        this.detalle.set(detalle);
        this.construirCalendario();
        this.cargandoSig.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.detalle.set(null);
        this.cargandoSig.set(false);
        this.errorSig.set(this.mensajeDeError(err));
      },
    });
  }

  reintentar(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    if (id) this.cargar(id);
  }

  private mensajeDeError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No fue posible conectar con el servidor. Intenta de nuevo.';
    if (err.status === 404) return 'El alojamiento no existe o no está disponible.';
    if (typeof err.error === 'string' && err.error.trim()) return err.error;
    return 'Ocurrió un error al cargar el alojamiento. Intenta de nuevo.';
  }

  get fechaMinimaHoy(): string {
    return this.formatearFecha(new Date());
  }

  get fechaMinimaSalida(): string {
    const fechaBaseStr = this.fechaLlegada || this.fechaMinimaHoy;
    const [anio, mes, dia] = fechaBaseStr.split('-').map(Number);
    const fechaSiguiente = new Date(anio, mes - 1, dia + 1);
    return this.formatearFecha(fechaSiguiente);
  }

  private formatearFecha(fecha: Date): string {
    const anio = fecha.getFullYear();
    const mes = String(fecha.getMonth() + 1).padStart(2, '0');
    const dia = String(fecha.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  get imagenSeleccionada() {
    if (this.indiceImagenSeleccionada !== null) {
      return this.alojamiento?.imagenes[this.indiceImagenSeleccionada] ?? null;
    }
    return null;
  }

  // Reseñas (CA43, CA44)

  get hayResenas(): boolean {
    return (this.alojamiento?.totalResenas ?? 0) > 0;
  }

  irAResenas(): void {
    document.getElementById('resenas')?.scrollIntoView({ behavior: 'smooth' });
  }

  // Disponibilidad

  get rangoNoDisponible(): boolean {
    if (!this.alojamiento || !this.fechaLlegada || !this.fechaSalida) return false;
    return this.alojamiento.calendarioOcupado.some(
      (r) => this.fechaLlegada < r.fechaFin && this.fechaSalida > r.fechaInicio,
    );
  }

  get huespedesInvalidos(): boolean {
    const cantidad = Number(this.cantidadHuespedes);
    return !(Number.isInteger(cantidad) && cantidad >= 1 && cantidad <= (this.alojamiento?.capacidad ?? 0));
  }

  /** Reservas hasta dos años adelante: evita años de 5 o 6 cifras escritos a mano. */
  get fechaMaxima(): string {
    const d = new Date();
    return this.formatearFecha(new Date(d.getFullYear() + 2, d.getMonth(), d.getDate()));
  }

  /** El campo de fecha permite escribir valores fuera de [min, max]; se validan aquí. */
  get fechasFueraDeRango(): boolean {
    const fueraLlegada =
      !!this.fechaLlegada && (this.fechaLlegada < this.fechaMinimaHoy || this.fechaLlegada > this.fechaMaxima);
    const fueraSalida = !!this.fechaSalida && this.fechaSalida > this.fechaMaxima;
    return fueraLlegada || fueraSalida;
  }

  get puedeReservar(): boolean {
    return (
      this.numeroNoches > 0 &&
      !this.fechasFueraDeRango &&
      !this.rangoNoDisponible &&
      !this.huespedesInvalidos
    );
  }

  get puedeIrMesAnterior(): boolean {
    const hoy = new Date();
    return (
      this.mesVisible.getFullYear() > hoy.getFullYear() ||
      this.mesVisible.getMonth() > hoy.getMonth()
    );
  }

  mesAnterior(): void {
    if (!this.puedeIrMesAnterior) return;
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() - 1, 1);
    this.construirCalendario();
  }

  mesSiguiente(): void {
    this.mesVisible = new Date(this.mesVisible.getFullYear(), this.mesVisible.getMonth() + 1, 1);
    this.construirCalendario();
  }

  private construirCalendario(): void {
    const anio = this.mesVisible.getFullYear();
    const mes = this.mesVisible.getMonth();
    const totalDias = new Date(anio, mes + 1, 0).getDate();
    const desfase = (new Date(anio, mes, 1).getDay() + 6) % 7;

    const celdas: (DiaCalendario | null)[] = Array(desfase).fill(null);
    for (let numero = 1; numero <= totalDias; numero++) {
      const fecha = this.formatearFecha(new Date(anio, mes, numero));
      celdas.push({ fecha, numero, estado: this.estadoDelDia(fecha) });
    }
    this.celdasCalendario = celdas;
  }

  private estadoDelDia(fecha: string): EstadoDia {
    if (fecha < this.fechaMinimaHoy) return 'PASADO';
    const ocupado = (this.alojamiento?.calendarioOcupado ?? []).some(
      (r) => fecha >= r.fechaInicio && fecha < r.fechaFin,
    );
    return ocupado ? 'OCUPADO' : 'LIBRE';
  }

  // Cotización (CA29, CA30)

  get numeroNoches(): number {
    if (!this.fechaLlegada || !this.fechaSalida) return 0;
    const inicio = new Date(this.fechaLlegada);
    const fin = new Date(this.fechaSalida);
    const diferencia = fin.getTime() - inicio.getTime();
    const dias = Math.ceil(diferencia / (1000 * 3600 * 24));
    return dias > 0 ? dias : 0;
  }

  get subtotalHospedaje(): number {
    return this.numeroNoches * (this.alojamiento?.precioNoche ?? 0);
  }

  get tarifaLimpieza(): number {
    return this.numeroNoches > 0 ? (this.alojamiento?.tarifaLimpieza ?? 0) : 0;
  }

  get tarifaServicio(): number {
    return Math.round((this.subtotalHospedaje * PORCENTAJE_SERVICIO) / 100);
  }

  get descuento(): number {
    if (this.numeroNoches < NOCHES_MINIMAS_DESCUENTO) return 0;
    return Math.round((this.subtotalHospedaje * PORCENTAJE_DESCUENTO) / 100);
  }

  get serviciosSeleccionados(): ServicioAlojamientoDTO[] {
    return this.serviciosAdicionales.filter((s) =>
      this.idsServiciosSeleccionados.includes(s.idServicio!),
    );
  }

  get totalServiciosAdicionales(): number {
    return this.serviciosSeleccionados.reduce((acc, s) => acc + this.costoServicio(s), 0);
  }

  get totalReserva(): number {
    if (this.numeroNoches <= 0) return 0;
    return (
      this.subtotalHospedaje +
      this.tarifaLimpieza +
      this.tarifaServicio +
      this.totalServiciosAdicionales -
      this.descuento
    );
  }

  // El costo de cada servicio adicional es por noche.
  costoServicio(servicio: ServicioAlojamientoDTO): number {
    return (servicio.costo ?? 0) * this.numeroNoches;
  }

  servicioSeleccionado(idServicio: number | undefined): boolean {
    if (idServicio == null) return false;
    return this.idsServiciosSeleccionados.includes(idServicio);
  }

  alternarServicio(idServicio: number | undefined): void {
    if (idServicio == null) return;
    if (this.servicioSeleccionado(idServicio)) {
      this.idsServiciosSeleccionados = this.idsServiciosSeleccionados.filter(
        (id) => id !== idServicio,
      );
    } else {
      this.idsServiciosSeleccionados = [...this.idsServiciosSeleccionados, idServicio];
    }
  }

  // Galería

  abrirImagenPorIndice(index: number) {
    this.indiceImagenSeleccionada = index;
  }

  cerrarModal() {
    this.indiceImagenSeleccionada = null;
  }

  imagenAnterior(event: Event) {
    event.stopPropagation();
    if (this.indiceImagenSeleccionada !== null && this.alojamiento) {
      this.indiceImagenSeleccionada =
        this.indiceImagenSeleccionada === 0
          ? this.alojamiento.imagenes.length - 1
          : this.indiceImagenSeleccionada - 1;
    }
  }

  imagenSiguiente(event: Event) {
    event.stopPropagation();
    if (this.indiceImagenSeleccionada !== null && this.alojamiento) {
      this.indiceImagenSeleccionada =
        (this.indiceImagenSeleccionada + 1) % this.alojamiento.imagenes.length;
    }
  }

  /**
   * Lleva al resumen de pago. Allí se pide la cotización real al back
   * (los valores de esta pantalla son solo una estimación).
   */
  reservarAhora(): void {
    if (!this.alojamiento || !this.puedeReservar) return;
    void this.router.navigate(['/resumen-reserva'], {
      queryParams: {
        id: this.alojamiento.idPublicacion,
        llegada: this.fechaLlegada,
        salida: this.fechaSalida,
        huespedes: Number(this.cantidadHuespedes),
        servicios: this.idsServiciosSeleccionados.length
          ? this.idsServiciosSeleccionados.join(',')
          : null,
      },
    });
  }
}
