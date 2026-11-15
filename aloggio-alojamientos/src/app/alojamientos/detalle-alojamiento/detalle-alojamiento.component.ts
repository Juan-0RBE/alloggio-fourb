import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface ServicioAdicional {
  id: number;
  nombre: string;
  precio: number;
}

@Component({
  selector: 'app-detalle-alojamiento',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './detalle-alojamiento.component.html',
  styleUrl: './detalle-alojamiento.component.scss',
})
export class DetalleAlojamiento {
  alojamiento = {
    titulo: 'Villa Moderna Vista al Valle y Montaña',
    ubicacion: 'Medellín, Antioquia',
    tipo: 'Villa completa',
    huespedes: 6,
    habitaciones: 3,
    banios: 2,
    calificacion: 4.95,
    resenasCount: 42,
    precioNoche: 550000,
    tarifaLimpieza: 85000,
    descripcion:
      'Hermosa villa privada con vista panorámica, ideal para descansar y disfrutar de la naturaleza con todas las comodidades modernas.',
    anfitrionNombre: 'Camila Restrepo',
    anfitrionAvatar:
      'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80',
    imagenes: [
      {
        url: 'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Vista exterior',
      },
      {
        url: 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Piscina',
      },
      {
        url: 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Sala',
      },
      {
        url: 'https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Cocina',
      },
      {
        url: 'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Habitación',
      },
    ],
    // Sin iconos fijos, completamente dinámico
    serviciosAdicionales: [
      { id: 1, nombre: 'Piscina climatizada', precio: 100000 },
      { id: 2, nombre: 'Wi-Fi de alta velocidad', precio: 40000 },
      { id: 3, nombre: 'Parqueadero gratuito', precio: 0 },
    ] as ServicioAdicional[],
  };

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

  cantidadHuespedes: number = 2;
  tarifaLimpieza: number = 85000;

  indiceImagenSeleccionada: number | null = null;

  get imagenSeleccionada() {
    if (this.indiceImagenSeleccionada !== null) {
      return this.alojamiento.imagenes[this.indiceImagenSeleccionada];
    }
    return null;
  }

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

  get totalServiciosAdicionales(): number {
    if (!this.alojamiento.serviciosAdicionales || this.numeroNoches <= 0) return 0;
    return this.alojamiento.serviciosAdicionales.reduce((acc, s) => acc + s.precio, 0);
  }

  get totalReserva(): number {
    return (
      this.subtotalHospedaje +
      (this.numeroNoches > 0 ? this.tarifaLimpieza : 0) +
      this.totalServiciosAdicionales
    );
  }

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
