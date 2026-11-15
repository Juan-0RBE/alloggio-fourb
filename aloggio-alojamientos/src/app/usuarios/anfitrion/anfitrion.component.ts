import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface ImagenAlojamientoAnfitrion {
  id: number;
  urlImagen: string;
}

interface AlojamientoAnfitrion {
  id: number;
  idAnfitrion: string;
  titulo: string;
  ubicacion: string;
  tipo: string;
  huespedes: number;
  habitaciones: number;
  banios: number;
  precioNoche: number;
  tarifaLimpieza: number;
  descripcion: string;
  imagenes: ImagenAlojamientoAnfitrion[];
  estado: 'Activo' | 'Pausado';
}

@Component({
  selector: 'app-gestion-anfitrion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './anfitrion.component.html',
  styleUrl: './anfitrion.component.scss',
})
export class Anfitrion {
  usuarioActualId = 'anfitrion-123';
  mostrandoModal = signal(false);
  editandoId = signal<number | null>(null);

  formTitulo = '';
  formUbicacion = '';
  formTipo = 'Villa completa';
  formHuespedes = 4;
  formHabitaciones = 2;
  formBanios = 2;
  formPrecioNoche = 500000;
  formTarifaLimpieza = 85000;
  formDescripcion = '';

  nuevaImagenUrl = '';
  formImagenes: string[] = [];

  alojamientos = signal<AlojamientoAnfitrion[]>([
    {
      id: 1,
      idAnfitrion: 'anfitrion-123',
      titulo: 'Villa Moderna Vista al Valle y Montaña',
      ubicacion: 'Medellín, Antioquia',
      tipo: 'Villa completa',
      huespedes: 6,
      habitaciones: 3,
      banios: 2.5,
      precioNoche: 550000,
      tarifaLimpieza: 85000,
      descripcion: 'Moderna villa con diseño Dark Premium, piscina privada y vista panorámica.',
      imagenes: [
        {
          id: 1,
          urlImagen:
            'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
        },
        {
          id: 2,
          urlImagen:
            'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=800&q=80',
        },
      ],
      estado: 'Activo',
    },
  ]);

  misAlojamientos = signal<AlojamientoAnfitrion[]>([]);

  constructor() {
    this.filtrarMisAlojamientos();
  }

  filtrarMisAlojamientos(): void {
    const propios = this.alojamientos().filter((a) => a.idAnfitrion === this.usuarioActualId);
    this.misAlojamientos.set(propios);
  }

  abrirModalCrear(): void {
    this.editandoId.set(null);
    this.formTitulo = '';
    this.formUbicacion = '';
    this.formTipo = 'Villa completa';
    this.formHuespedes = 4;
    this.formHabitaciones = 2;
    this.formBanios = 2;
    this.formPrecioNoche = 450000;
    this.formTarifaLimpieza = 80000;
    this.formDescripcion = '';
    this.nuevaImagenUrl = '';
    this.formImagenes = [
      'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80',
    ];
    this.mostrandoModal.set(true);
  }

  abrirModalEditar(aloj: AlojamientoAnfitrion): void {
    this.editandoId.set(aloj.id);
    this.formTitulo = aloj.titulo;
    this.formUbicacion = aloj.ubicacion;
    this.formTipo = aloj.tipo;
    this.formHuespedes = aloj.huespedes;
    this.formHabitaciones = aloj.habitaciones;
    this.formBanios = aloj.banios;
    this.formPrecioNoche = aloj.precioNoche;
    this.formTarifaLimpieza = aloj.tarifaLimpieza;
    this.formDescripcion = aloj.descripcion;
    this.nuevaImagenUrl = '';
    this.formImagenes = aloj.imagenes.map((img) => img.urlImagen);
    this.mostrandoModal.set(true);
  }

  cerrarModal(): void {
    this.mostrandoModal.set(false);
  }

  agregarImagenForm(): void {
    if (this.nuevaImagenUrl.trim()) {
      this.formImagenes.push(this.nuevaImagenUrl.trim());
      this.nuevaImagenUrl = '';
    }
  }

  eliminarImagenForm(index: number): void {
    this.formImagenes.splice(index, 1);
  }

  guardarAlojamiento(): void {
    if (!this.formTitulo.trim() || !this.formUbicacion.trim()) {
      alert('Por favor completa los campos principales.');
      return;
    }

    if (this.formImagenes.length === 0) {
      alert('Agrega al menos una imagen para la galería del alojamiento.');
      return;
    }

    const idEditando = this.editandoId();

    if (idEditando !== null) {
      this.alojamientos.update((lista) =>
        lista.map((item) =>
          item.id === idEditando
            ? {
                ...item,
                titulo: this.formTitulo,
                ubicacion: this.formUbicacion,
                tipo: this.formTipo,
                huespedes: Number(this.formHuespedes),
                habitaciones: Number(this.formHabitaciones),
                banios: Number(this.formBanios),
                precioNoche: Number(this.formPrecioNoche),
                tarifaLimpieza: Number(this.formTarifaLimpieza),
                descripcion: this.formDescripcion,
                imagenes: this.formImagenes.map((url, index) => ({
                  id: index + 1,
                  urlImagen: url,
                })),
              }
            : item,
        ),
      );
    } else {
      const nuevoAlojamiento: AlojamientoAnfitrion = {
        id: Date.now(),
        idAnfitrion: this.usuarioActualId,
        titulo: this.formTitulo,
        ubicacion: this.formUbicacion,
        tipo: this.formTipo,
        huespedes: Number(this.formHuespedes),
        habitaciones: Number(this.formHabitaciones),
        banios: Number(this.formBanios),
        precioNoche: Number(this.formPrecioNoche),
        tarifaLimpieza: Number(this.formTarifaLimpieza),
        descripcion: this.formDescripcion,
        imagenes: this.formImagenes.map((url, index) => ({ id: index + 1, urlImagen: url })),
        estado: 'Activo',
      };

      this.alojamientos.update((lista) => [nuevoAlojamiento, ...lista]);
    }

    this.filtrarMisAlojamientos();
    this.cerrarModal();
  }

  eliminarAlojamiento(id: number): void {
    if (confirm('¿Estás seguro de eliminar este alojamiento?')) {
      this.alojamientos.update((lista) => lista.filter((item) => item.id !== id));
      this.filtrarMisAlojamientos();
    }
  }

  alternarEstado(id: number): void {
    this.alojamientos.update((lista) =>
      lista.map((item) =>
        item.id === id
          ? { ...item, estado: item.estado === 'Activo' ? 'Pausado' : 'Activo' }
          : item,
      ),
    );
    this.filtrarMisAlojamientos();
  }
}
