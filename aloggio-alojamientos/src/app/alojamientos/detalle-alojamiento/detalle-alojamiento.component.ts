import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface ImagenAlojamiento {
  id: number;
  url: string;
  descripcion: string;
}

interface ServicioIncluido {
  id: number;
  nombre: string;
  icono: string;
  incluido: boolean;
}

interface AlojamientoDetalle {
  id: number;
  titulo: string;
  ubicacion: string;
  tipo: string;
  huespedes: number;
  habitaciones: number;
  banios: number;
  calificacion: number;
  resenasCount: number;
  precioNoche: number;
  descripcion: string;
  anfitrionNombre: string;
  anfitrionAvatar: string;
  esSuperanfitrion: boolean;
  imagenes: ImagenAlojamiento[];
  servicios: ServicioIncluido[];
  reglas: string[];
  politicaCancelacion: string;
}

@Component({
  imports: [CommonModule, FormsModule, RouterModule],
  selector: 'app-detalle-alojamiento',
  styleUrl: './detalle-alojamiento.component.scss',
  templateUrl: './detalle-alojamiento.component.html',
})
export class DetalleAlojamiento {
  alojamiento: AlojamientoDetalle = {
    id: 1,
    titulo: '',
    ubicacion: '',
    tipo: '',
    huespedes: 1,
    habitaciones: 1,
    banios: 1,
    calificacion: 1,
    resenasCount: 1,
    precioNoche: 1,
    descripcion: '',
    anfitrionNombre: '',
    anfitrionAvatar: '',
    esSuperanfitrion: true,
    imagenes: [],
    servicios: [],
    reglas: [],
    politicaCancelacion: '',
  };

  fechaLlegada: string = '';
  fechaSalida: string = '';
  cantidadHuespedes: number = 2;
  tarifaLimpieza: number = 85000;

  indiceImagenSeleccionada: number | null = null;

  get imagenSeleccionada(): ImagenAlojamiento | null {
    if (this.indiceImagenSeleccionada !== null) {
      return this.alojamiento.imagenes[this.indiceImagenSeleccionada];
    }
    return null;
  }

  get numeroNoches(): number {
    if (!this.fechaLlegada || !this.fechaSalida) return 0;
    const inicio = new Date(this.fechaLlegada);
    const fin = new Date(this.fechaSalida);
    const diferenciaTiempo = fin.getTime() - inicio.getTime();
    const dias = Math.ceil(diferenciaTiempo / (1000 * 3600 * 24));
    return dias > 0 ? dias : 0;
  }

  get subtotalHospedaje(): number {
    return this.numeroNoches * this.alojamiento.precioNoche;
  }

  get totalReserva(): number {
    if (this.numeroNoches <= 0) return 0;
    return this.subtotalHospedaje + this.tarifaLimpieza;
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
}
