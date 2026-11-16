import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface ImagenAlojamientoAnfitrion {
  id: number;
  urlImagen: string;
}

interface ServicioAdicional {
  id: number;
  nombre: string;
  precio: number;
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
  serviciosAdicionales: ServicioAdicional[];
  estado: 'Activo' | 'Pausado';
}

@Component({
  selector: 'app-gestion-anfitrion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './anfitrion.html',
  styleUrl: './anfitrion.scss',
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

  nuevoServicioNombre = '';
  nuevoServicioPrecio = 50000;
  formServicios: { nombre: string; precio: number }[] = [];

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
      ],
      serviciosAdicionales: [
        { id: 1, nombre: 'Piscina climatizada', precio: 100000 },
        { id: 2, nombre: 'WiFi de alta velocidad', precio: 40000 },
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

    this.nuevoServicioNombre = '';
    this.nuevoServicioPrecio = 50000;
    this.formServicios = [
      { nombre: 'WiFi Alta Velocidad', precio: 30000 },
      { nombre: 'Uso de Jacuzzi / Piscina', precio: 80000 },
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

    this.formServicios = aloj.serviciosAdicionales.map((s) => ({
      nombre: s.nombre,
      precio: s.precio,
    }));
    this.nuevoServicioNombre = '';
    this.nuevoServicioPrecio = 50000;

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

  agregarServicioForm(): void {
    if (this.nuevoServicioNombre.trim()) {
      this.formServicios.push({
        nombre: this.nuevoServicioNombre.trim(),
        precio: Number(this.nuevoServicioPrecio) || 0,
      });
      this.nuevoServicioNombre = '';
      this.nuevoServicioPrecio = 50000;
    }
  }

  eliminarServicioForm(index: number): void {
    this.formServicios.splice(index, 1);
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
    const serviciosMapeados: ServicioAdicional[] = this.formServicios.map((s, idx) => ({
      id: idx + 1,
      nombre: s.nombre,
      precio: s.precio,
    }));

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
                serviciosAdicionales: serviciosMapeados,
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
        serviciosAdicionales: serviciosMapeados,
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

  get totalServiciosForm(): number {
    return this.formServicios.reduce((total, serv) => total + (Number(serv.precio) || 0), 0);
  }

  get precioConTodosLosServiciosForm(): number {
    const base = Number(this.formPrecioNoche) || 0;
    const limpieza = Number(this.formTarifaLimpieza) || 0;
    return base + limpieza + this.totalServiciosForm;
  }

  calcularTotalCompletoTarjeta(aloj: AlojamientoAnfitrion): number {
    const totalServicios = (aloj.serviciosAdicionales || []).reduce((acc, s) => acc + s.precio, 0);
    return aloj.precioNoche + (aloj.tarifaLimpieza || 0) + totalServicios;
  }
}
