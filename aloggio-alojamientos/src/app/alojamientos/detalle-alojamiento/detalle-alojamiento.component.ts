import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { Estrella } from '../../resenas/estrella/estrella.component';
import { FormularioResena } from '../../resenas/formulario-resena/formulario-resena.component';

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

interface ResenaItem {
  id: number;
  nombreUsuario: string;
  avatarUrl: string;
  fechaEstancia: string;
  calificacion: number;
  comentario: string;
}

@Component({
  imports: [CommonModule, FormsModule, RouterModule, Estrella, FormularioResena],
  selector: 'app-detalle-alojamiento',
  styleUrl: './detalle-alojamiento.component.scss',
  templateUrl: './detalle-alojamiento.component.html',
})
export class DetalleAlojamiento implements OnInit {
  alojamiento: AlojamientoDetalle = {
    id: 1,
    titulo: 'Villa Moderna Vista al Valle y Montaña',
    ubicacion: 'Medellín, Antioquia, Colombia',
    tipo: 'Villa completa',
    huespedes: 6,
    habitaciones: 3,
    banios: 2.5,
    calificacion: 4.95,
    resenasCount: 42,
    precioNoche: 550000,
    descripcion:
      'Disfruta de una experiencia única en esta moderna villa ubicada en las colinas de Medellín. Diseñada con un concepto abierto, amplios ventanales de vidrio y acabados Dark Premium de lujo. Cuenta con piscina privada climatizada, zona BBQ, internet de alta velocidad ideal para nómadas digitales y una vista panorámica impresionante de todo el valle.',
    anfitrionNombre: 'Camila Andrés Restrepo',
    anfitrionAvatar:
      'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=150&q=80',
    esSuperanfitrion: true,
    imagenes: [
      {
        id: 1,
        url: 'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Vista exterior de la villa',
      },
      {
        id: 2,
        url: 'https://images.unsplash.com/photo-1584622650111-993a426fbf0a?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Piscina y terraza',
      },
      {
        id: 3,
        url: 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Sala principal',
      },
      {
        id: 4,
        url: 'https://images.unsplash.com/photo-1600565193348-f74bd3c7ccdf?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Cocina moderna',
      },
      {
        id: 5,
        url: 'https://images.unsplash.com/photo-1616594039964-ae9021a400a0?auto=format&fit=crop&w=800&q=80',
        descripcion: 'Habitación principal',
      },
    ],
    servicios: [
      { id: 1, nombre: 'Piscina privada', icono: 'pi pi-sun', incluido: true },
      { id: 2, nombre: 'Wi-Fi de alta velocidad', icono: 'pi pi-wifi', incluido: true },
      {
        id: 3,
        nombre: 'Parqueadero gratuito en las instalaciones',
        icono: 'pi pi-car',
        incluido: true,
      },
      { id: 4, nombre: 'Cocina totalmente equipada', icono: 'pi pi-briefcase', incluido: true },
      { id: 5, nombre: 'Televisor Smart TV 65"', icono: 'pi pi-desktop', incluido: true },
      { id: 6, nombre: 'Aire acondicionado', icono: 'pi pi-bolt', incluido: true },
    ],
    reglas: [
      'No se permiten fiestas ni eventos masivos',
      'Horario de silencio: 10:00 PM a 8:00 AM',
      'No se admiten mascotas',
      'Check-in: de 3:00 PM a 9:00 PM',
    ],
    politicaCancelacion:
      'Cancelación gratuita hasta 48 horas antes de la fecha de llegada. Pasado ese plazo, la primera noche no es reembolsable.',
  };

  fechaLlegada: string = '';
  fechaSalida: string = '';
  cantidadHuespedes: number = 2;
  tarifaLimpieza: number = 85000;

  indiceImagenSeleccionada: number | null = null;

  mostrandoModalResena: boolean = false;
  totalResenas: number = 42;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const idParam = params.get('id');
      if (idParam) {
        console.log('Cargando detalles para el alojamiento ID:', idParam);
      }
    });
  }

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

  listaResenas: ResenaItem[] = [
    {
      id: 1,
      nombreUsuario: 'Alejandro Morales',
      avatarUrl:
        'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80',
      fechaEstancia: 'Noviembre de 2026',
      calificacion: 5,
      comentario:
        '¡Una experiencia increíble! La vista superó totalmente nuestras expectativas y el anfitrión estuvo pendiente de cada detalle. Sin duda volveremos.',
    },
    {
      id: 2,
      nombreUsuario: 'Daniela Giraldo',
      avatarUrl:
        'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=150&q=80',
      fechaEstancia: 'Octubre de 2026',
      calificacion: 5,
      comentario:
        'El lugar es exactamente como se ve en las fotos. Muy limpio, organizado y la zona es sumamente tranquila para descansar el fin de semana.',
    },
  ];

  abrirModalResena(): void {
    this.mostrandoModalResena = true;
  }

  cerrarModalResena(): void {
    this.mostrandoModalResena = false;
  }

  guardarResena(nuevaResenaData: any): void {
    this.listaResenas.unshift({
      id: Date.now(),
      nombreUsuario: 'Tú (Usuario Actual)',
      avatarUrl:
        'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=150&q=80',
      fechaEstancia: 'Diciembre de 2026',
      calificacion: nuevaResenaData.calificacionGeneral,
      comentario: nuevaResenaData.comentario,
    });

    this.totalResenas++;
    this.alojamiento.resenasCount = this.totalResenas;
    this.mostrandoModalResena = false;
    alert('¡Gracias por tu reseña! Ha sido publicada con éxito.');
  }
}
