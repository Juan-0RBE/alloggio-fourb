import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';

import { PublicacionService } from '../../../service/publicacion.service';
import { SERVICIOS_CATALOGO, claveTexto, normalizarServicio } from '../../../model/servicios-catalogo';
import {
  EstadoPublicacion,
  PoliticaCancelacionDTO,
  PublicacionDTO,
  TipoAlojamiento,
} from '../../../model/publicacion.model';

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

type PoliticaCancelacion = PoliticaCancelacionDTO;

interface AlojamientoAnfitrion {
  id: number;
  idAnfitrion: number | null;
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
export class Anfitrion implements OnInit {
  private publicacionService = inject(PublicacionService);

  readonly etiquetasEstado = ETIQUETAS_ESTADO;
  readonly tiposAlojamiento = TIPOS_ALOJAMIENTO;
  readonly serviciosCatalogo = SERVICIOS_CATALOGO;
  errorServicio = signal('');

  mostrandoModal = signal(false);
  editandoId = signal<number | null>(null);
  errorFormulario = signal('');
  aviso = signal<AvisoAccion | null>(null);
  cargando = signal(false);
  guardando = signal(false);

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

  politicas = signal<PoliticaCancelacion[]>([]);
  misAlojamientos = signal<AlojamientoAnfitrion[]>([]);

  ngOnInit(): void {
    this.cargarPoliticas();
    this.cargarMisAlojamientos();
  }

  // Carga de datos desde el back

  cargarPoliticas(): void {
    this.publicacionService.obtenerPoliticas().subscribe({
      next: (lista) => this.politicas.set(lista),
      error: (err) => this.mostrarError(err, 'No se pudieron cargar las políticas de cancelación.'),
    });
  }

  cargarMisAlojamientos(): void {
    this.cargando.set(true);
    this.publicacionService
      .obtenerMisPublicaciones()
      .pipe(
        switchMap((publicaciones) =>
          publicaciones.length === 0
            ? of([] as AlojamientoAnfitrion[])
            : forkJoin(publicaciones.map((pub) => this.cargarDetalle(pub))),
        ),
      )
      .subscribe({
        next: (lista) => {
          // Más recientes primero
          this.misAlojamientos.set([...lista].sort((a, b) => b.id - a.id));
          this.cargando.set(false);
        },
        error: (err) => {
          this.cargando.set(false);
          this.mostrarError(err, 'No se pudieron cargar tus alojamientos.');
        },
      });
  }

  /** Junta la publicación con sus imágenes, servicios y reglas. */
  private cargarDetalle(pub: PublicacionDTO): Observable<AlojamientoAnfitrion> {
    const id = pub.idPublicacion!;
    return forkJoin({
      imagenes: this.publicacionService.obtenerImagenes(id),
      servicios: this.publicacionService.obtenerServicios(id),
      reglas: this.publicacionService.obtenerReglas(id),
    }).pipe(
      map(({ imagenes, servicios, reglas }) => ({
        id,
        idAnfitrion: pub.idAnfitrion ?? null,
        titulo: pub.titulo ?? '',
        descripcion: pub.descripcion ?? '',
        ubicacion: pub.ubicacion ?? '',
        ciudad: pub.ciudad ?? '',
        tipo: pub.tipo,
        capacidad: pub.capacidad ?? 0,
        precioNoche: pub.precioNoche ?? 0,
        tarifaLimpieza: pub.tarifaLimpieza ?? 0,
        idPoliticaCancelacion: pub.idPoliticaCancelacion ?? null,
        estado: pub.estado ?? 'BORRADOR',
        motivoBloqueo: pub.motivoBloqueo ?? null,
        imagenes: [...imagenes]
          .sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0))
          .map((img) => ({ id: img.idImagen!, urlImagen: img.urlImagen })),
        serviciosAdicionales: servicios.map((s) => ({
          id: s.idServicio!,
          nombre: s.nombre,
          costo: s.costo ?? 0,
        })),
        reglas: reglas.map((r) => ({ id: r.idRegla!, descripcion: r.descripcion })),
      })),
    );
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
    this.errorServicio.set('');
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
    this.errorServicio.set('');
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
    // Se guarda con el nombre del catálogo para que coincida con el filtro de búsqueda
    const nombre = normalizarServicio(this.nuevoServicioNombre);
    const costo = Number(this.nuevoServicioCosto);
    if (!nombre) {
      this.errorServicio.set('Escribe el nombre del servicio.');
      return;
    }
    if (nombre.length > 50) {
      this.errorServicio.set('El nombre del servicio puede tener máximo 50 caracteres.');
      return;
    }
    if (!Number.isFinite(costo) || costo < 0) {
      this.errorServicio.set('El costo debe ser un valor de cero o mayor.');
      return;
    }
    if (this.formServicios.some((s) => claveTexto(s.nombre) === claveTexto(nombre))) {
      this.errorServicio.set(`El servicio "${nombre}" ya está en la lista.`);
      return;
    }
    this.errorServicio.set('');
    this.formServicios.push({ nombre, costo });
    this.nuevoServicioNombre = '';
    this.nuevoServicioCosto = 50000;
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

  private construirDTO(): PublicacionDTO {
    return {
      titulo: this.formTitulo.trim(),
      descripcion: this.formDescripcion.trim(),
      ciudad: this.formCiudad.trim(),
      ubicacion: this.formUbicacion.trim(),
      tipo: this.formTipo,
      capacidad: Number(this.formCapacidad),
      precioNoche: Number(this.formPrecioNoche),
      tarifaLimpieza: Number(this.formTarifaLimpieza),
      idPoliticaCancelacion: this.formIdPolitica,
    };
  }

  guardarAlojamiento(): void {
    if (this.guardando()) return;

    const invalidos = this.camposInvalidosForm();
    if (invalidos.length > 0) {
      this.errorFormulario.set(
        `Faltan campos obligatorios o son inválidos: ${invalidos.join(', ')}.`,
      );
      return;
    }
    this.errorFormulario.set('');
    this.guardando.set(true);

    const idEditando = this.editandoId();
    const dto = this.construirDTO();

    const operacion$ =
      idEditando !== null ? this.guardarEdicion(idEditando, dto) : this.guardarNuevo(dto);

    operacion$.subscribe({
      next: () => {
        this.guardando.set(false);
        this.aviso.set({
          tipo: 'exito',
          texto:
            idEditando !== null
              ? 'Cambios guardados.'
              : 'Publicación guardada como borrador. Actívala cuando esté lista.',
        });
        this.cerrarModal();
        this.cargarMisAlojamientos();
      },
      error: (err) => {
        this.guardando.set(false);
        // El error se muestra dentro del modal para que el anfitrión pueda corregir
        this.errorFormulario.set(this.textoError(err, 'No se pudo guardar el alojamiento.'));
        // Puede que una parte sí se haya guardado (p. ej. la publicación pero no una imagen)
        this.cargarMisAlojamientos();
      },
    });
  }

  /**
   * Crea la publicación y luego sus imágenes, servicios y reglas.
   * El back solo responde texto al crear, así que el id se obtiene
   * buscando la publicación más reciente del anfitrión.
   */
  private guardarNuevo(dto: PublicacionDTO): Observable<unknown> {
    return this.publicacionService.crear(dto).pipe(
      switchMap(() => this.publicacionService.obtenerMisPublicaciones()),
      switchMap((lista) => {
        const ids = lista.map((p) => p.idPublicacion ?? 0);
        const idNuevo = Math.max(0, ...ids);
        if (!idNuevo) throw new Error('No se pudo identificar la publicación creada.');
        return this.guardarHijos(
          idNuevo,
          this.formImagenes,
          this.formServicios,
          this.formReglas,
          0,
        );
      }),
    );
  }

  /**
   * Actualiza los datos básicos y sincroniza imágenes, servicios y reglas:
   * elimina los que el anfitrión quitó y crea los nuevos.
   */
  private guardarEdicion(id: number, dto: PublicacionDTO): Observable<unknown> {
    const original = this.misAlojamientos().find((a) => a.id === id);

    return this.publicacionService.actualizar(id, dto).pipe(
      switchMap(() => {
        if (!original) {
          return this.guardarHijos(id, this.formImagenes, this.formServicios, this.formReglas, 0);
        }

        // Imágenes
        const imagenesBorrar = this.diferencia(
          original.imagenes,
          this.formImagenes,
          (img, url) => img.urlImagen === url,
        );
        const imagenesNuevas = this.diferencia(
          this.formImagenes,
          original.imagenes,
          (url, img) => url === img.urlImagen,
        );

        // Servicios
        const serviciosBorrar = this.diferencia(
          original.serviciosAdicionales,
          this.formServicios,
          (a, b) => a.nombre === b.nombre && Number(a.costo) === Number(b.costo),
        );
        const serviciosNuevos = this.diferencia(
          this.formServicios,
          original.serviciosAdicionales,
          (a, b) => a.nombre === b.nombre && Number(a.costo) === Number(b.costo),
        );

        // Reglas
        const reglasBorrar = this.diferencia(
          original.reglas,
          this.formReglas,
          (r, texto) => r.descripcion === texto,
        );
        const reglasNuevas = this.diferencia(
          this.formReglas,
          original.reglas,
          (texto, r) => texto === r.descripcion,
        );

        const eliminaciones = [
          ...imagenesBorrar.map((img) => this.publicacionService.eliminarImagen(img.id)),
          ...serviciosBorrar.map((s) => this.publicacionService.eliminarServicio(s.id)),
          ...reglasBorrar.map((r) => this.publicacionService.eliminarRegla(r.id)),
        ];

        const ordenInicial = original.imagenes.length - imagenesBorrar.length;

        const eliminar$: Observable<unknown> = eliminaciones.length
          ? forkJoin(eliminaciones)
          : of(null);

        return eliminar$.pipe(
          switchMap(() =>
            this.guardarHijos(id, imagenesNuevas, serviciosNuevos, reglasNuevas, ordenInicial),
          ),
        );
      }),
    );
  }

  /** Crea en el back las imágenes, servicios y reglas indicados. */
  private guardarHijos(
    idPublicacion: number,
    imagenes: string[],
    servicios: { nombre: string; costo: number }[],
    reglas: string[],
    ordenInicial: number,
  ): Observable<unknown> {
    const peticiones: Observable<string>[] = [
      ...imagenes.map((url, idx) =>
        this.publicacionService.agregarImagen(idPublicacion, {
          urlImagen: url,
          orden: ordenInicial + idx + 1,
        }),
      ),
      ...servicios.map((s) =>
        this.publicacionService.agregarServicio(idPublicacion, {
          nombre: s.nombre,
          costo: Number(s.costo) || 0,
          esAdicional: true,
        }),
      ),
      ...reglas.map((texto) =>
        this.publicacionService.agregarRegla(idPublicacion, { descripcion: texto }),
      ),
    ];
    return peticiones.length ? forkJoin(peticiones) : of(null);
  }

  /**
   * Devuelve los elementos de `origen` que no tienen pareja en `contra`
   * (respetando duplicados: cada elemento de `contra` solo empareja una vez).
   */
  private diferencia<A, B>(origen: A[], contra: B[], iguales: (a: A, b: B) => boolean): A[] {
    const disponibles = [...contra];
    return origen.filter((item) => {
      const idx = disponibles.findIndex((otro) => iguales(item, otro));
      if (idx === -1) return true;
      disponibles.splice(idx, 1);
      return false;
    });
  }

  eliminarAlojamiento(aloj: AlojamientoAnfitrion): void {
    if (!this.puedeEditar(aloj)) return;
    if (!confirm('¿Estás seguro de eliminar este alojamiento?')) return;

    this.publicacionService.eliminar(aloj.id).subscribe({
      next: () => {
        this.misAlojamientos.update((lista) => lista.filter((item) => item.id !== aloj.id));
        this.aviso.set({ tipo: 'exito', texto: 'Alojamiento eliminado.' });
      },
      error: (err) => this.mostrarError(err, 'No se pudo eliminar el alojamiento.'),
    });
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

    // Validación rápida en el front; el back vuelve a validar
    const faltantes = this.datosFaltantes(aloj);
    if (faltantes.length > 0) {
      this.aviso.set({
        tipo: 'error',
        texto: `La publicación no cumple la información mínima. Faltan: ${faltantes.join(', ')}.`,
      });
      return;
    }

    this.publicacionService.activar(aloj.id).subscribe({
      next: () => {
        this.cambiarEstadoLocal(aloj.id, 'ACTIVA');
        this.aviso.set({
          tipo: 'exito',
          texto: 'Publicación activada. Ya aparece en las búsquedas.',
        });
      },
      error: (err) => this.mostrarError(err, 'No se pudo activar la publicación.'),
    });
  }

  pausarAlojamiento(aloj: AlojamientoAnfitrion): void {
    if (aloj.estado !== 'ACTIVA') return;

    this.publicacionService.pausar(aloj.id).subscribe({
      next: () => {
        this.cambiarEstadoLocal(aloj.id, 'PAUSADA');
        this.aviso.set({
          tipo: 'exito',
          texto: 'Publicación pausada. Las reservas futuras confirmadas se conservan.',
        });
      },
      error: (err) => this.mostrarError(err, 'No se pudo pausar la publicación.'),
    });
  }

  private cambiarEstadoLocal(id: number, nuevoEstado: EstadoPublicacion): void {
    this.misAlojamientos.update((lista) =>
      lista.map((item) => (item.id === id ? { ...item, estado: nuevoEstado } : item)),
    );
  }

  // Manejo de errores

  private textoError(err: unknown, porDefecto: string): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 0) return 'No hay conexión con el servidor.';
      if (typeof err.error === 'string' && err.error.trim()) return err.error;
      if (err.error?.message) return err.error.message;
    } else if (err instanceof Error && err.message) {
      return err.message;
    }
    return porDefecto;
  }

  private mostrarError(err: unknown, porDefecto: string): void {
    this.aviso.set({ tipo: 'error', texto: this.textoError(err, porDefecto) });
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
