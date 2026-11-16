import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type TipoAlojamiento = 'CASA' | 'APARTAMENTO' | 'HABITACION' | 'CABAÑA';
type EstadoDia = 'LIBRE' | 'OCUPADO' | 'PASADO';

interface ServicioVista {
  idServicio: number;
  nombre: string;
  costo: number;
  esAdicional: boolean;
}

interface ReglaVista {
  idRegla: number;
  descripcion: string;
}

interface ImagenVista {
  idImagen: number;
  urlImagen: string;
  orden: number;
}

interface RangoOcupado {
  fechaInicio: string;
  fechaFin: string;
}

interface ResenaVista {
  idResena: number;
  autor: string;
  calificacion: number;
  comentario: string;
  fecha: string;
}

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
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './detalle-alojamiento.html',
  styleUrl: './detalle-alojamiento.scss',
})
export class DetalleAlojamiento {
  readonly etiquetasTipo = ETIQUETAS_TIPO;
  readonly estrellas = [1, 2, 3, 4, 5];
  readonly diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  alojamiento = {
    idPublicacion: 1,
    titulo: 'Villa Moderna Vista al Valle y Montaña',
    descripcion:
      'Hermosa villa privada con vista panorámica, ideal para descansar y disfrutar de la naturaleza con todas las comodidades modernas.',
    ubicacion: 'Vereda El Retiro, km 3',
    ciudad: 'Medellín',
    tipo: 'CASA' as TipoAlojamiento,
    capacidad: 6,
    precioNoche: 550000,
    tarifaLimpieza: 85000,
    politicaCancelacion: {
      idPolitica: 1,
      nombre: 'Flexible',
      porcentajeDevolucion: 80,
      plazoCancelacion: 2,
      condiciones: 'Devolución del 80% si cancelas con al menos 2 días de anticipación.',
    },
    servicios: [
      { idServicio: 1, nombre: 'Piscina climatizada', costo: 100000, esAdicional: true },
      { idServicio: 2, nombre: 'WiFi de alta velocidad', costo: 40000, esAdicional: true },
      { idServicio: 3, nombre: 'Parqueadero gratuito', costo: 0, esAdicional: false },
      { idServicio: 4, nombre: 'Cocina equipada', costo: 0, esAdicional: false },
    ] as ServicioVista[],
    reglas: [
      { idRegla: 1, descripcion: 'No se permiten fiestas ni eventos.' },
      { idRegla: 2, descripcion: 'Silencio después de las 10 p. m.' },
      { idRegla: 3, descripcion: 'No se admiten mascotas.' },
    ] as ReglaVista[],
    imagenes: [
      {
        idImagen: 1,
        urlImagen:
          'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
        orden: 1,
      },
      {
        idImagen: 2,
        urlImagen:
          'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=800&q=80',
        orden: 2,
      },
      {
        idImagen: 3,
        urlImagen:
          'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80',
        orden: 3,
      },
      {
        idImagen: 4,
        urlImagen:
          'https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?auto=format&fit=crop&w=800&q=80',
        orden: 4,
      },
      {
        idImagen: 5,
        urlImagen:
          'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=800&q=80',
        orden: 5,
      },
    ] as ImagenVista[],
    calendarioOcupado: [
      { fechaInicio: '2026-12-10', fechaFin: '2026-12-15' },
      { fechaInicio: '2027-01-05', fechaFin: '2027-01-08' },
    ] as RangoOcupado[],
    promedioCalificacion: 4.7,
    totalResenas: 3,
    resenas: [
      {
        idResena: 88,
        autor: 'sofia.martinez',
        calificacion: 5,
        comentario: 'Excelente estadía, la vista es espectacular y todo estaba impecable.',
        fecha: '2026-11-05',
      },
      {
        idResena: 85,
        autor: 'camilo.duarte',
        calificacion: 5,
        comentario: 'La piscina y la cocina estaban perfectas para ir en familia.',
        fecha: '2026-10-18',
      },
      {
        idResena: 80,
        autor: 'laura.gomez',
        calificacion: 4,
        comentario: 'Muy bonita y tranquila. El acceso en carro es un poco empinado.',
        fecha: '2026-09-30',
      },
    ] as ResenaVista[],
  };

  readonly serviciosIncluidos = this.alojamiento.servicios.filter((s) => !s.esAdicional);
  readonly serviciosAdicionales = this.alojamiento.servicios.filter((s) => s.esAdicional);

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

  constructor() {
    this.construirCalendario();
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
      return this.alojamiento.imagenes[this.indiceImagenSeleccionada];
    }
    return null;
  }

  // Reseñas (CA43, CA44)

  get hayResenas(): boolean {
    return this.alojamiento.totalResenas > 0;
  }

  irAResenas(): void {
    document.getElementById('resenas')?.scrollIntoView({ behavior: 'smooth' });
  }

  // Disponibilidad

  get rangoNoDisponible(): boolean {
    if (!this.fechaLlegada || !this.fechaSalida) return false;
    return this.alojamiento.calendarioOcupado.some(
      (r) => this.fechaLlegada < r.fechaFin && this.fechaSalida > r.fechaInicio,
    );
  }

  get huespedesInvalidos(): boolean {
    const cantidad = Number(this.cantidadHuespedes);
    return !(cantidad >= 1 && cantidad <= this.alojamiento.capacidad);
  }

  get puedeReservar(): boolean {
    return this.numeroNoches > 0 && !this.rangoNoDisponible && !this.huespedesInvalidos;
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
    const ocupado = this.alojamiento.calendarioOcupado.some(
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
    return this.numeroNoches * this.alojamiento.precioNoche;
  }

  get tarifaLimpieza(): number {
    return this.numeroNoches > 0 ? this.alojamiento.tarifaLimpieza : 0;
  }

  get tarifaServicio(): number {
    return Math.round((this.subtotalHospedaje * PORCENTAJE_SERVICIO) / 100);
  }

  get descuento(): number {
    if (this.numeroNoches < NOCHES_MINIMAS_DESCUENTO) return 0;
    return Math.round((this.subtotalHospedaje * PORCENTAJE_DESCUENTO) / 100);
  }

  get serviciosSeleccionados(): ServicioVista[] {
    return this.serviciosAdicionales.filter((s) =>
      this.idsServiciosSeleccionados.includes(s.idServicio),
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
  costoServicio(servicio: ServicioVista): number {
    return servicio.costo * this.numeroNoches;
  }

  servicioSeleccionado(idServicio: number): boolean {
    return this.idsServiciosSeleccionados.includes(idServicio);
  }

  alternarServicio(idServicio: number): void {
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
    if (this.indiceImagenSeleccionada !== null) {
      this.indiceImagenSeleccionada =
        this.indiceImagenSeleccionada === 0
          ? this.alojamiento.imagenes.length - 1
          : this.indiceImagenSeleccionada - 1;
    }
  }

  imagenSiguiente(event: Event) {
    event.stopPropagation();
    if (this.indiceImagenSeleccionada !== null) {
      this.indiceImagenSeleccionada =
        (this.indiceImagenSeleccionada + 1) % this.alojamiento.imagenes.length;
    }
  }

  reservarAhora() {
    alert('¡Reserva simulada con éxito! Redirigiendo al resumen de pago...');
  }
}
