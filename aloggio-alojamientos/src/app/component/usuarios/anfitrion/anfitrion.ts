import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type EstadoPublicacion = 'BORRADOR' | 'ACTIVA' | 'PAUSADA' | 'BLOQUEADA';
type TipoAlojamiento = 'CASA' | 'APARTAMENTO' | 'HABITACION' | 'CABAÑA';

interface ImagenAlojamientoAnfitrion {
  id: number;
  urlImagen: string;
}

interface ServicioAdicional {
  id: number;
  nombre: string;
  costo: number;
}

interface ReglaAlojamiento {
  id: number;
  descripcion: string;
}

interface PoliticaCancelacion {
  idPolitica: number;
  nombre: string;
  porcentajeDevolucion: number;
  plazoCancelacion: number;
  condiciones: string;
}

interface AlojamientoAnfitrion {
  id: number;
  idAnfitrion: string;
  titulo: string;
  descripcion: string;
  ubicacion: string;
  ciudad: string;
  tipo: TipoAlojamiento;
  capacidad: number;
  precioNoche: number;
  tarifaLimpieza: number;
  idPoliticaCancelacion: number | null;
  estado: EstadoPublicacion;
  motivoBloqueo: string | null;
  imagenes: ImagenAlojamientoAnfitrion[];
  serviciosAdicionales: ServicioAdicional[];
  reglas: ReglaAlojamiento[];
}

interface AvisoAccion {
  tipo: 'exito' | 'error';
  texto: string;
}

export const ETIQUETAS_ESTADO: Record<EstadoPublicacion, string> = {
  BORRADOR: 'Borrador',
  ACTIVA: 'Activa',
  PAUSADA: 'Pausada',
  BLOQUEADA: 'Bloqueada',
};

export const TIPOS_ALOJAMIENTO: { valor: TipoAlojamiento; etiqueta: string }[] = [
  { valor: 'CASA', etiqueta: 'Casa' },
  { valor: 'APARTAMENTO', etiqueta: 'Apartamento' },
  { valor: 'HABITACION', etiqueta: 'Habitación' },
  { valor: 'CABAÑA', etiqueta: 'Cabaña' },
];

@Component({
  selector: 'app-gestion-anfitrion',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './anfitrion.html',
  styleUrl: './anfitrion.scss',
})
export class Anfitrion {
  readonly etiquetasEstado = ETIQUETAS_ESTADO;
  readonly tiposAlojamiento = TIPOS_ALOJAMIENTO;

  usuarioActualId = 'anfitrion-123';
  mostrandoModal = signal(false);
  editandoId = signal<number | null>(null);
  errorFormulario = signal('');
  aviso = signal<AvisoAccion | null>(null);

  formTitulo = '';
  formDescripcion = '';
  formCiudad = '';
  formUbicacion = '';
  formTipo: TipoAlojamiento = 'CASA';
  formCapacidad = 4;
  formPrecioNoche = 500000;
  formTarifaLimpieza = 85000;
  formIdPolitica: number | null = null;

  nuevaImagenUrl = '';
  formImagenes: string[] = [];

  nuevoServicioNombre = '';
  nuevoServicioCosto = 50000;
  formServicios: { nombre: string; costo: number }[] = [];

  nuevaReglaTexto = '';
  formReglas: string[] = [];

  politicas = signal<PoliticaCancelacion[]>([
    {
      idPolitica: 1,
      nombre: 'Flexible',
      porcentajeDevolucion: 80,
      plazoCancelacion: 2,
      condiciones: 'Devolución del 80% si cancelas con al menos 2 días de anticipación.',
    },
    {
      idPolitica: 2,
      nombre: 'Moderada',
      porcentajeDevolucion: 50,
      plazoCancelacion: 5,
      condiciones: 'Devolución del 50% si cancelas con al menos 5 días de anticipación.',
    },
    {
      idPolitica: 3,
      nombre: 'Estricta',
      porcentajeDevolucion: 20,
      plazoCancelacion: 14,
      condiciones: 'Devolución del 20% si cancelas con al menos 14 días de anticipación.',
    },
  ]);

  alojamientos = signal<AlojamientoAnfitrion[]>([
    {
      id: 1,
      idAnfitrion: 'anfitrion-123',
      titulo: 'Villa Moderna Vista al Valle y Montaña',
      descripcion: 'Moderna villa con diseño Dark Premium, piscina privada y vista panorámica.',
      ubicacion: 'Vereda El Retiro, km 3',
      ciudad: 'Medellín',
      tipo: 'CASA',
      capacidad: 6,
      precioNoche: 550000,
      tarifaLimpieza: 85000,
      idPoliticaCancelacion: 1,
      estado: 'ACTIVA',
      motivoBloqueo: null,
      imagenes: [
        {
          id: 1,
          urlImagen:
            'https://images.unsplash.com/photo-1613977257363-707ba9348227?auto=format&fit=crop&w=800&q=80',
        },
      ],
      serviciosAdicionales: [
        { id: 1, nombre: 'Piscina climatizada', costo: 100000 },
        { id: 2, nombre: 'WiFi de alta velocidad', costo: 40000 },
      ],
      reglas: [
        { id: 1, descripcion: 'No se permiten fiestas ni eventos.' },
        { id: 2, descripcion: 'Silencio después de las 10 p. m.' },
      ],
    },
    {
      id: 2,
      idAnfitrion: 'anfitrion-123',
      titulo: 'Cabaña Alpina en el Bosque',
      descripcion: 'Cabaña rodeada de bosque, ideal para desconectarse.',
      ubicacion: 'Sector Los Pinos',
      ciudad: 'Guatapé',
      tipo: 'CABAÑA',
      capacidad: 4,
      precioNoche: 350000,
      tarifaLimpieza: 60000,
      idPoliticaCancelacion: 2,
      estado: 'BORRADOR',
      motivoBloqueo: null,
      imagenes: [],
      serviciosAdicionales: [],
      reglas: [],
    },
    {
      id: 3,
      idAnfitrion: 'anfitrion-123',
      titulo: 'Habitación en el centro histórico',
      descripcion: 'Habitación privada a pasos de la ciudad amurallada.',
      ubicacion: 'Calle de la Moneda',
      ciudad: 'Cartagena',
      tipo: 'HABITACION',
      capacidad: 2,
      precioNoche: 180000,
      tarifaLimpieza: 30000,
      idPoliticaCancelacion: 3,
      estado: 'BLOQUEADA',
      motivoBloqueo: 'Las fotos no corresponden al alojamiento publicado.',
      imagenes: [
        {
          id: 1,
          urlImagen:
            'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&w=800&q=80',
        },
      ],
      serviciosAdicionales: [],
      reglas: [],
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

  etiquetaTipo(tipo: TipoAlojamiento): string {
    return this.tiposAlojamiento.find((t) => t.valor === tipo)?.etiqueta ?? tipo;
  }

  politicaDe(id: number | null): PoliticaCancelacion | undefined {
    return this.politicas().find((p) => p.idPolitica === id);
  }

  puedeEditar(aloj: AlojamientoAnfitrion): boolean {
    return aloj.estado !== 'BLOQUEADA';
  }

  cerrarAviso(): void {
    this.aviso.set(null);
  }

  // Formulario

  abrirModalCrear(): void {
    this.editandoId.set(null);
    this.errorFormulario.set('');
    this.formTitulo = '';
    this.formDescripcion = '';
    this.formCiudad = '';
    this.formUbicacion = '';
    this.formTipo = 'CASA';
    this.formCapacidad = 4;
    this.formPrecioNoche = 450000;
    this.formTarifaLimpieza = 80000;
    this.formIdPolitica = null;

    this.nuevaImagenUrl = '';
    this.formImagenes = [];

    this.nuevoServicioNombre = '';
    this.nuevoServicioCosto = 50000;
    this.formServicios = [];

    this.nuevaReglaTexto = '';
    this.formReglas = [];

    this.mostrandoModal.set(true);
  }

  abrirModalEditar(aloj: AlojamientoAnfitrion): void {
    if (!this.puedeEditar(aloj)) return;

    this.editandoId.set(aloj.id);
    this.errorFormulario.set('');
    this.formTitulo = aloj.titulo;
    this.formDescripcion = aloj.descripcion;
    this.formCiudad = aloj.ciudad;
    this.formUbicacion = aloj.ubicacion;
    this.formTipo = aloj.tipo;
    this.formCapacidad = aloj.capacidad;
    this.formPrecioNoche = aloj.precioNoche;
    this.formTarifaLimpieza = aloj.tarifaLimpieza;
    this.formIdPolitica = aloj.idPoliticaCancelacion;

    this.nuevaImagenUrl = '';
    this.formImagenes = aloj.imagenes.map((img) => img.urlImagen);

    this.formServicios = aloj.serviciosAdicionales.map((s) => ({
      nombre: s.nombre,
      costo: s.costo,
    }));
    this.nuevoServicioNombre = '';
    this.nuevoServicioCosto = 50000;

    this.formReglas = aloj.reglas.map((r) => r.descripcion);
    this.nuevaReglaTexto = '';

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
        costo: Number(this.nuevoServicioCosto) || 0,
      });
      this.nuevoServicioNombre = '';
      this.nuevoServicioCosto = 50000;
    }
  }

  eliminarServicioForm(index: number): void {
    this.formServicios.splice(index, 1);
  }

  agregarReglaForm(): void {
    if (this.nuevaReglaTexto.trim()) {
      this.formReglas.push(this.nuevaReglaTexto.trim());
      this.nuevaReglaTexto = '';
    }
  }

  eliminarReglaForm(index: number): void {
    this.formReglas.splice(index, 1);
  }

  camposInvalidosForm(): string[] {
    const invalidos: string[] = [];
    if (!this.formTitulo.trim()) invalidos.push('título');
    if (!this.formDescripcion.trim()) invalidos.push('descripción');
    if (!this.formCiudad.trim()) invalidos.push('ciudad');
    if (!this.formUbicacion.trim()) invalidos.push('ubicación');
    if (!this.formTipo) invalidos.push('tipo de alojamiento');
    if (!(Number(this.formCapacidad) > 0)) invalidos.push('capacidad (mayor que cero)');
    if (!(Number(this.formPrecioNoche) > 0)) invalidos.push('precio por noche (positivo)');
    if (!(Number(this.formTarifaLimpieza) >= 0))
      invalidos.push('tarifa de limpieza (cero o mayor)');
    if (this.formIdPolitica === null) invalidos.push('política de cancelación');
    return invalidos;
  }

  guardarAlojamiento(): void {
    const invalidos = this.camposInvalidosForm();
    if (invalidos.length > 0) {
      this.errorFormulario.set(
        `Faltan campos obligatorios o son inválidos: ${invalidos.join(', ')}.`,
      );
      return;
    }
    this.errorFormulario.set('');

    const idEditando = this.editandoId();
    const imagenes = this.formImagenes.map((url, index) => ({ id: index + 1, urlImagen: url }));
    const serviciosAdicionales = this.formServicios.map((s, idx) => ({
      id: idx + 1,
      nombre: s.nombre,
      costo: s.costo,
    }));
    const reglas = this.formReglas.map((texto, idx) => ({ id: idx + 1, descripcion: texto }));

    if (idEditando !== null) {
      this.alojamientos.update((lista) =>
        lista.map((item) =>
          item.id === idEditando
            ? {
                ...item,
                titulo: this.formTitulo.trim(),
                descripcion: this.formDescripcion.trim(),
                ciudad: this.formCiudad.trim(),
                ubicacion: this.formUbicacion.trim(),
                tipo: this.formTipo,
                capacidad: Number(this.formCapacidad),
                precioNoche: Number(this.formPrecioNoche),
                tarifaLimpieza: Number(this.formTarifaLimpieza),
                idPoliticaCancelacion: this.formIdPolitica,
                imagenes,
                serviciosAdicionales,
                reglas,
              }
            : item,
        ),
      );
      this.aviso.set({ tipo: 'exito', texto: 'Cambios guardados.' });
    } else {
      const nuevoAlojamiento: AlojamientoAnfitrion = {
        id: Date.now(),
        idAnfitrion: this.usuarioActualId,
        titulo: this.formTitulo.trim(),
        descripcion: this.formDescripcion.trim(),
        ciudad: this.formCiudad.trim(),
        ubicacion: this.formUbicacion.trim(),
        tipo: this.formTipo,
        capacidad: Number(this.formCapacidad),
        precioNoche: Number(this.formPrecioNoche),
        tarifaLimpieza: Number(this.formTarifaLimpieza),
        idPoliticaCancelacion: this.formIdPolitica,
        estado: 'BORRADOR',
        motivoBloqueo: null,
        imagenes,
        serviciosAdicionales,
        reglas,
      };

      this.alojamientos.update((lista) => [nuevoAlojamiento, ...lista]);
      this.aviso.set({
        tipo: 'exito',
        texto: 'Publicación guardada como borrador. Actívala cuando esté lista.',
      });
    }

    this.filtrarMisAlojamientos();
    this.cerrarModal();
  }

  eliminarAlojamiento(aloj: AlojamientoAnfitrion): void {
    if (!this.puedeEditar(aloj)) return;
    if (confirm('¿Estás seguro de eliminar este alojamiento?')) {
      this.alojamientos.update((lista) => lista.filter((item) => item.id !== aloj.id));
      this.filtrarMisAlojamientos();
      this.aviso.set({ tipo: 'exito', texto: 'Alojamiento eliminado.' });
    }
  }

  // Estados de la publicación

  datosFaltantes(aloj: AlojamientoAnfitrion): string[] {
    const faltantes: string[] = [];
    if (!aloj.titulo.trim()) faltantes.push('título');
    if (!aloj.descripcion.trim()) faltantes.push('descripción');
    if (!aloj.ubicacion.trim()) faltantes.push('ubicación');
    if (!aloj.ciudad.trim()) faltantes.push('ciudad');
    if (!aloj.tipo) faltantes.push('tipo de alojamiento');
    if (!(aloj.capacidad > 0)) faltantes.push('capacidad (mayor que cero)');
    if (!(aloj.precioNoche > 0)) faltantes.push('precio por noche (positivo)');
    if (aloj.imagenes.length === 0) faltantes.push('al menos una imagen');
    return faltantes;
  }

  activarAlojamiento(aloj: AlojamientoAnfitrion): void {
    if (aloj.estado !== 'BORRADOR' && aloj.estado !== 'PAUSADA') return;

    const faltantes = this.datosFaltantes(aloj);
    if (faltantes.length > 0) {
      this.aviso.set({
        tipo: 'error',
        texto: `La publicación no cumple la información mínima. Faltan: ${faltantes.join(', ')}.`,
      });
      return;
    }

    this.cambiarEstado(aloj.id, 'ACTIVA');
    this.aviso.set({ tipo: 'exito', texto: 'Publicación activada. Ya aparece en las búsquedas.' });
  }

  pausarAlojamiento(aloj: AlojamientoAnfitrion): void {
    if (aloj.estado !== 'ACTIVA') return;

    this.cambiarEstado(aloj.id, 'PAUSADA');
    this.aviso.set({
      tipo: 'exito',
      texto: 'Publicación pausada. Las reservas futuras confirmadas se conservan.',
    });
  }

  private cambiarEstado(id: number, nuevoEstado: EstadoPublicacion): void {
    this.alojamientos.update((lista) =>
      lista.map((item) => (item.id === id ? { ...item, estado: nuevoEstado } : item)),
    );
    this.filtrarMisAlojamientos();
  }

  // Totales

  get politicaSeleccionadaForm(): PoliticaCancelacion | undefined {
    return this.politicaDe(this.formIdPolitica);
  }

  get totalServiciosForm(): number {
    return this.formServicios.reduce((total, serv) => total + (Number(serv.costo) || 0), 0);
  }

  get precioConTodosLosServiciosForm(): number {
    const base = Number(this.formPrecioNoche) || 0;
    const limpieza = Number(this.formTarifaLimpieza) || 0;
    return base + limpieza + this.totalServiciosForm;
  }

  calcularTotalCompletoTarjeta(aloj: AlojamientoAnfitrion): number {
    const totalServicios = (aloj.serviciosAdicionales || []).reduce((acc, s) => acc + s.costo, 0);
    return aloj.precioNoche + (aloj.tarifaLimpieza || 0) + totalServicios;
  }
}
