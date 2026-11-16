import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../../service/admin.service';

type SeccionAdmin =
  'dashboard' | 'reportes' | 'publicaciones' | 'resenas' | 'usuarios' | 'auditoria';

type EstadoPublicacion = 'ACTIVA' | 'PAUSADA' | 'BLOQUEADA';
type EstadoCuenta = 'ACTIVA' | 'RESTRINGIDA';
type ResultadoAuditoria = 'EXITO' | 'FALLIDO' | 'DENEGADO';
type FormatoExportacion = 'PDF' | 'EXCEL';

interface ReservaFutura {
  idReserva: number;
  fechaLlegada: string;
  fechaSalida: string;
  total: number;
  nombreHuesped: string;
}

interface PublicacionMod {
  idPublicacion: number;
  titulo: string;
  ciudad: string;
  precioNoche: number;
  estado: EstadoPublicacion;
  motivoBloqueo: string | null;
  nombreAnfitrion: string;
  totalReservasFuturas: number;
  reservasFuturas: ReservaFutura[];
}

interface ResenaMod {
  idResena: number;
  idPublicacion: number;
  autor: string;
  calificacion: number;
  comentario: string;
  fecha: string;
  visible: boolean;
}

interface UsuarioMod {
  idUsuario: number;
  nombreUsuario: string;
  primerNombre: string;
  primerApellido: string;
  role: 'USUARIO' | 'ADMINISTRADOR';
  estadoCuenta: EstadoCuenta;
}

interface EventoAuditoriaVista {
  idAuditoria: number;
  correoUsuario: string;
  accion: string;
  entidadTipo: string;
  entidadId: number | null;
  fechaHora: string;
  descripcion: string;
  resultado: ResultadoAuditoria;
}

interface CiudadDemanda {
  ciudad: string;
  reservas: number;
  ingresos: number;
}

interface DashboardVista {
  totalUsuarios: number;
  usuariosPorEstadoCuenta: Record<string, number>;
  totalPublicaciones: number;
  publicacionesActivas: number;
  publicacionesPorEstado: Record<string, number>;
  totalReservas: number;
  reservasPorEstado: Record<string, number>;
  ingresosEstimados: number;
  ciudadesDemanda: CiudadDemanda[];
  sinDatos: boolean;
}

interface FiltrosAuditoria {
  fechaDesde: string;
  fechaHasta: string;
  correoUsuario: string;
  accion: string;
  entidadTipo: string;
  resultado: string;
}

const LONGITUD_MAX_MOTIVO = 500;
const TAMANO_PAGINA = 10;
const MAX_EVENTOS_AUDITORIA = 100;

const ACCIONES_AUDITORIA = [
  'REGISTRO_USUARIO',
  'INICIO_SESION',
  'SOLICITUD_RECUPERACION',
  'RESTABLECER_CONTRASENA',
  'ACTUALIZAR_PERFIL',
  'DESACTIVAR_CUENTA',
  'ACTIVAR_CUENTA',
  'CREAR_PUBLICACION',
  'ACTUALIZAR_PUBLICACION',
  'ELIMINAR_PUBLICACION',
  'ACTIVAR_PUBLICACION',
  'PAUSAR_PUBLICACION',
  'BLOQUEAR_PUBLICACION',
  'BLOQUEAR_RANGO',
  'DESBLOQUEAR_RANGO',
  'PROCESAR_PAGO',
  'CREAR_RESERVA',
  'CANCELAR_RESERVA',
  'CREAR_RESENA',
  'OCULTAR_RESENA',
  'RESTRINGIR_USUARIO',
  'EXPORTAR_INDICADORES',
];

const TIPOS_ENTIDAD = [
  'USUARIO',
  'PUBLICACION',
  'PERIODO_DISPONIBILIDAD',
  'RESERVA',
  'PAGO',
  'RESENA',
  'REPORTE',
];

const FILTROS_AUDITORIA_VACIOS: FiltrosAuditoria = {
  fechaDesde: '',
  fechaHasta: '',
  correoUsuario: '',
  accion: '',
  entidadTipo: '',
  resultado: '',
};

const DASHBOARD_VACIO: DashboardVista = {
  totalUsuarios: 0,
  usuariosPorEstadoCuenta: {},
  totalPublicaciones: 0,
  publicacionesActivas: 0,
  publicacionesPorEstado: {},
  totalReservas: 0,
  reservasPorEstado: {},
  ingresosEstimados: 0,
  ciudadesDemanda: [],
  sinDatos: true,
};

@Component({
  selector: 'app-panel-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin implements OnInit {
  private adminService = inject(AdminService);

  readonly longitudMaxMotivo = LONGITUD_MAX_MOTIVO;
  readonly accionesAuditoria = ACCIONES_AUDITORIA;
  readonly tiposEntidad = TIPOS_ENTIDAD;

  adminActual = signal('');

  seccionActiva = signal<SeccionAdmin>('dashboard');
  mensajeConfirmacion = signal('');
  mensajeError = signal('');

  // Dashboard y exportación (CA51, CA52, CA53)

  // Mientras llegan los datos del backend se muestran los contadores en cero
  dashboard = signal<DashboardVista>({ ...DASHBOARD_VACIO, sinDatos: false });
  cargandoDashboard = signal(false);
  exportando = signal(false);

  reservasPorEstado = computed(() => Object.entries(this.dashboard().reservasPorEstado));

  formatoExportacion: FormatoExportacion = 'PDF';
  exportaciones = signal<EventoAuditoriaVista[]>([]);

  // Moderación de publicaciones (CA46, CA47)

  filtroEstadoPublicacion = signal<'TODAS' | EstadoPublicacion>('TODAS');
  publicacionSeleccionada = signal<PublicacionMod | null>(null);
  errorBloqueo = signal('');
  motivoBloqueo = '';
  bloqueando = signal(false);

  publicaciones = signal<PublicacionMod[]>([]);
  cargandoPublicaciones = signal(false);

  publicacionesFiltradas = computed(() => {
    const estado = this.filtroEstadoPublicacion();
    if (estado === 'TODAS') return this.publicaciones();
    return this.publicaciones().filter((p) => p.estado === estado);
  });

  // Moderación de reseñas (CA45)

  alojamientoSeleccionado = signal<number | null>(null);
  resenaEnConfirmacion = signal<number | null>(null);
  resenas = signal<ResenaMod[]>([]);
  cargandoResenas = signal(false);

  // Las reseñas cargadas siempre corresponden al alojamiento seleccionado
  resenasDelAlojamiento = computed(() => this.resenas());

  // Gestión de usuarios (CA48)

  busquedaUsuario = signal('');
  filtroEstadoCuenta = signal<'TODOS' | EstadoCuenta>('TODOS');
  usuarioEnConfirmacion = signal<number | null>(null);
  usuarios = signal<UsuarioMod[]>([]);
  cargandoUsuarios = signal(false);

  usuariosFiltrados = computed(() => {
    const termino = this.busquedaUsuario().trim().toLowerCase();
    const estado = this.filtroEstadoCuenta();
    return this.usuarios().filter((u) => {
      const nombreCompleto = `${u.primerNombre} ${u.primerApellido}`.toLowerCase();
      const coincideTexto =
        !termino ||
        nombreCompleto.includes(termino) ||
        u.nombreUsuario.toLowerCase().includes(termino) ||
        String(u.idUsuario).includes(termino);
      const coincideEstado = estado === 'TODOS' || u.estadoCuenta === estado;
      return coincideTexto && coincideEstado;
    });
  });

  // Log de auditoría (CA49, CA50)

  filtroFechaDesde = '';
  filtroFechaHasta = '';
  filtroCorreo = '';
  filtroAccion = '';
  filtroEntidad = '';
  filtroResultado = '';

  filtrosAplicados = signal<FiltrosAuditoria>({ ...FILTROS_AUDITORIA_VACIOS });
  errorFiltros = signal('');
  pagina = signal(0);

  eventosAuditoria = signal<EventoAuditoriaVista[]>([]);
  cargandoAuditoria = signal(false);

  // Las fechas, acción, entidad y resultado se filtran en el backend;
  // el correo se filtra aquí para admitir coincidencias parciales
  eventosFiltrados = computed(() => {
    const correo = this.filtrosAplicados().correoUsuario.trim().toLowerCase();
    return this.eventosAuditoria().filter(
      (ev) => !correo || ev.correoUsuario.toLowerCase().includes(correo),
    );
  });

  totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.eventosFiltrados().length / TAMANO_PAGINA)),
  );

  eventosPagina = computed(() => {
    const inicio = this.pagina() * TAMANO_PAGINA;
    return this.eventosFiltrados().slice(inicio, inicio + TAMANO_PAGINA);
  });

  ngOnInit(): void {
    this.adminService.getPerfil().subscribe({
      next: (perfil) => this.adminActual.set(perfil?.correoUsuario ?? perfil?.nombreUsuario ?? ''),
      error: () => this.adminActual.set(''),
    });
    this.cargarDashboard();
  }

  // Cada sección carga sus datos al abrirse
  cambiarSeccion(seccion: SeccionAdmin): void {
    this.mensajeConfirmacion.set('');
    this.mensajeError.set('');
    this.seccionActiva.set(seccion);

    switch (seccion) {
      case 'dashboard':
        this.cargarDashboard();
        break;
      case 'reportes':
        this.cargarExportaciones();
        break;
      case 'publicaciones':
        this.cargarPublicaciones();
        break;
      case 'resenas':
        this.cargarPublicaciones(() => this.seleccionarPrimerAlojamiento());
        break;
      case 'usuarios':
        this.cargarUsuarios();
        break;
      case 'auditoria':
        this.cargarAuditoria();
        break;
    }
  }

  // Dashboard y exportación

  cargarDashboard(): void {
    this.cargandoDashboard.set(true);
    this.adminService.getIndicadores().subscribe({
      next: (res) => {
        this.dashboard.set({
          totalUsuarios: res.totalUsuarios ?? 0,
          usuariosPorEstadoCuenta: res.usuariosPorEstadoCuenta ?? {},
          totalPublicaciones: res.totalPublicaciones ?? 0,
          publicacionesActivas: res.publicacionesActivas ?? 0,
          publicacionesPorEstado: res.publicacionesPorEstado ?? {},
          totalReservas: res.totalReservas ?? 0,
          reservasPorEstado: res.reservasPorEstado ?? {},
          ingresosEstimados: Number(res.ingresosEstimados ?? 0),
          ciudadesDemanda: (res.ciudadesDemanda ?? []).map((c: any) => ({
            ciudad: c.ciudad,
            reservas: c.reservas,
            ingresos: Number(c.ingresos ?? 0),
          })),
          sinDatos: !!res.sinDatos,
        });
        this.cargandoDashboard.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargandoDashboard.set(false);
        this.mensajeError.set(this.textoError(err, 'No se pudieron cargar los indicadores.'));
      },
    });
  }

  // Solo para demostrar el estado vacío (CA52)
  simularDashboardVacio(): void {
    this.dashboard.set({ ...DASHBOARD_VACIO });
  }

  exportarIndicadores(formato: FormatoExportacion): void {
    if (this.exportando()) return;
    this.exportando.set(true);
    this.mensajeConfirmacion.set('');
    this.mensajeError.set('');

    this.adminService.exportarIndicadores(formato).subscribe({
      next: (archivo: Blob) => {
        this.descargarArchivo(archivo, formato);
        this.mensajeConfirmacion.set(
          `Exportación en ${formato} generada. Quedó registrada en el log de auditoría.`,
        );
        this.exportando.set(false);
        this.cargarExportaciones();
      },
      error: async (err: HttpErrorResponse) => {
        // Con responseType 'blob', el mensaje de error del backend llega como Blob
        let detalle = '';
        if (err.error instanceof Blob) {
          try {
            detalle = await err.error.text();
          } catch {
            detalle = '';
          }
        }
        this.mensajeError.set(
          detalle || this.textoError(err, `No se pudo exportar el archivo ${formato}.`),
        );
        this.exportando.set(false);
      },
    });
  }

  // El historial de exportaciones son los eventos de auditoría EXPORTAR_INDICADORES
  cargarExportaciones(): void {
    this.adminService
      .consultarAuditoria({ accion: 'EXPORTAR_INDICADORES', pagina: 0, tamano: 20 })
      .subscribe({
        next: (lista) => this.exportaciones.set((lista ?? []).map((e) => this.aEventoVista(e))),
        error: (err: HttpErrorResponse) =>
          this.mensajeError.set(this.textoError(err, 'No se pudo cargar el historial.')),
      });
  }

  private descargarArchivo(archivo: Blob, formato: FormatoExportacion): void {
    const extension = formato === 'PDF' ? 'pdf' : 'xlsx';
    const url = URL.createObjectURL(archivo);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = `indicadores.${extension}`;
    enlace.click();
    URL.revokeObjectURL(url);
  }

  // Publicaciones

  cargarPublicaciones(alTerminar?: () => void): void {
    this.cargandoPublicaciones.set(true);
    this.adminService.listarPublicaciones().subscribe({
      next: (lista) => {
        this.publicaciones.set(
          (lista ?? []).map((p: any) => ({
            idPublicacion: p.idPublicacion,
            titulo: p.titulo ?? '',
            ciudad: p.ciudad ?? '',
            precioNoche: Number(p.precioNoche ?? 0),
            estado: p.estado,
            motivoBloqueo: p.motivoBloqueo ?? null,
            nombreAnfitrion: p.nombreAnfitrion || p.nombreUsuarioAnfitrion || '',
            totalReservasFuturas: p.totalReservasFuturas ?? 0,
            reservasFuturas: (p.reservasFuturas ?? []).map((r: any) => ({
              idReserva: r.idReserva,
              fechaLlegada: r.fechaLlegada,
              fechaSalida: r.fechaSalida,
              total: Number(r.total ?? 0),
              nombreHuesped: r.nombreHuesped || r.nombreUsuarioHuesped || '',
            })),
          })),
        );
        this.cargandoPublicaciones.set(false);
        alTerminar?.();
      },
      error: (err: HttpErrorResponse) => {
        this.cargandoPublicaciones.set(false);
        this.mensajeError.set(this.textoError(err, 'No se pudieron cargar las publicaciones.'));
      },
    });
  }

  seleccionarFiltroPublicacion(estado: 'TODAS' | EstadoPublicacion): void {
    this.filtroEstadoPublicacion.set(estado);
  }

  puedeBloquearse(pub: PublicacionMod): boolean {
    return pub.estado === 'ACTIVA' || pub.estado === 'PAUSADA';
  }

  abrirRevision(pub: PublicacionMod): void {
    this.motivoBloqueo = '';
    this.errorBloqueo.set('');
    this.publicacionSeleccionada.set(pub);
  }

  cerrarRevision(): void {
    this.publicacionSeleccionada.set(null);
  }

  bloquearPublicacion(): void {
    const pub = this.publicacionSeleccionada();
    if (!pub || !this.puedeBloquearse(pub) || this.bloqueando()) return;

    const motivo = this.motivoBloqueo.trim();
    if (motivo.length === 0 || motivo.length > LONGITUD_MAX_MOTIVO) {
      this.errorBloqueo.set(
        `El motivo es obligatorio y admite máximo ${LONGITUD_MAX_MOTIVO} caracteres.`,
      );
      return;
    }

    this.bloqueando.set(true);
    this.adminService.bloquearPublicacion(pub.idPublicacion, motivo).subscribe({
      next: () => {
        this.publicaciones.update((lista) =>
          lista.map((p) =>
            p.idPublicacion === pub.idPublicacion
              ? { ...p, estado: 'BLOQUEADA' as EstadoPublicacion, motivoBloqueo: motivo }
              : p,
          ),
        );
        this.bloqueando.set(false);
        this.publicacionSeleccionada.set(null);
        this.mensajeConfirmacion.set(
          `Publicación bloqueada. Reservas futuras conservadas para revisión manual: ${pub.totalReservasFuturas}.`,
        );
      },
      error: (err: HttpErrorResponse) => {
        this.bloqueando.set(false);
        this.errorBloqueo.set(this.textoError(err, 'No se pudo bloquear la publicación.'));
      },
    });
  }

  // Reseñas

  private seleccionarPrimerAlojamiento(): void {
    const actual = this.alojamientoSeleccionado();
    const existe = this.publicaciones().some((p) => p.idPublicacion === actual);
    if (existe && actual !== null) {
      this.cargarResenas(actual);
      return;
    }
    const primera = this.publicaciones()[0];
    if (primera) {
      this.seleccionarAlojamiento(primera.idPublicacion);
    } else {
      this.alojamientoSeleccionado.set(null);
      this.resenas.set([]);
    }
  }

  seleccionarAlojamiento(idPublicacion: number): void {
    this.resenaEnConfirmacion.set(null);
    this.alojamientoSeleccionado.set(idPublicacion);
    this.cargarResenas(idPublicacion);
  }

  private cargarResenas(idPublicacion: number): void {
    this.cargandoResenas.set(true);
    this.adminService.listarResenas(idPublicacion).subscribe({
      next: (lista) => {
        // Se ignora la respuesta si el administrador ya eligió otro alojamiento
        if (this.alojamientoSeleccionado() !== idPublicacion) return;
        this.resenas.set(
          (lista ?? []).map((r: any) => ({
            idResena: r.idResena,
            idPublicacion,
            autor: r.autor ?? '',
            calificacion: r.calificacion,
            comentario: r.comentario ?? '',
            fecha: r.fecha,
            visible: r.visible ?? true,
          })),
        );
        this.cargandoResenas.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargandoResenas.set(false);
        this.mensajeError.set(this.textoError(err, 'No se pudieron cargar las reseñas.'));
      },
    });
  }

  pedirConfirmacionOcultar(resena: ResenaMod): void {
    this.resenaEnConfirmacion.set(resena.idResena);
  }

  cancelarOcultar(): void {
    this.resenaEnConfirmacion.set(null);
  }

  ocultarResena(resena: ResenaMod): void {
    if (!resena.visible) return;
    this.mensajeError.set('');

    this.adminService.ocultarResena(resena.idResena).subscribe({
      next: () => {
        this.resenas.update((lista) =>
          lista.map((r) => (r.idResena === resena.idResena ? { ...r, visible: false } : r)),
        );
        this.resenaEnConfirmacion.set(null);
        this.mensajeConfirmacion.set(
          'Reseña ocultada. Ya no se muestra en el detalle del alojamiento.',
        );
      },
      error: (err: HttpErrorResponse) => {
        this.resenaEnConfirmacion.set(null);
        this.mensajeError.set(this.textoError(err, 'No se pudo ocultar la reseña.'));
      },
    });
  }

  // Usuarios

  cargarUsuarios(): void {
    this.cargandoUsuarios.set(true);
    this.adminService.listarUsuarios().subscribe({
      next: (lista) => {
        this.usuarios.set(
          (lista ?? []).map((u: any) => ({
            idUsuario: u.idUsuario,
            nombreUsuario: u.nombreUsuario ?? '',
            primerNombre: u.primerNombre ?? '',
            primerApellido: u.primerApellido ?? '',
            role: u.role,
            estadoCuenta: u.estadoCuenta,
          })),
        );
        this.cargandoUsuarios.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargandoUsuarios.set(false);
        this.mensajeError.set(this.textoError(err, 'No se pudieron cargar los usuarios.'));
      },
    });
  }

  seleccionarFiltroEstadoCuenta(estado: 'TODOS' | EstadoCuenta): void {
    this.filtroEstadoCuenta.set(estado);
  }

  // El backend no permite restringir administradores (ni al propio), por eso basta con el rol
  puedeRestringirse(usuario: UsuarioMod): boolean {
    return usuario.role !== 'ADMINISTRADOR' && usuario.estadoCuenta !== 'RESTRINGIDA';
  }

  pedirConfirmacionRestriccion(usuario: UsuarioMod): void {
    this.usuarioEnConfirmacion.set(usuario.idUsuario);
  }

  cancelarRestriccion(): void {
    this.usuarioEnConfirmacion.set(null);
  }

  restringirUsuario(usuario: UsuarioMod): void {
    if (!this.puedeRestringirse(usuario)) return;
    this.mensajeError.set('');

    this.adminService.restringirUsuario(usuario.idUsuario).subscribe({
      next: () => {
        this.usuarios.update((lista) =>
          lista.map((u) =>
            u.idUsuario === usuario.idUsuario
              ? { ...u, estadoCuenta: 'RESTRINGIDA' as EstadoCuenta }
              : u,
          ),
        );
        this.usuarioEnConfirmacion.set(null);
        this.mensajeConfirmacion.set(
          'Cuenta restringida. El usuario ya no puede iniciar sesión ni realizar nuevas acciones.',
        );
      },
      error: (err: HttpErrorResponse) => {
        this.usuarioEnConfirmacion.set(null);
        this.mensajeError.set(this.textoError(err, 'No se pudo restringir la cuenta.'));
      },
    });
  }

  // Auditoría

  cargarAuditoria(): void {
    const f = this.filtrosAplicados();
    this.cargandoAuditoria.set(true);
    this.adminService
      .consultarAuditoria({
        fechaDesde: f.fechaDesde,
        fechaHasta: f.fechaHasta,
        accion: f.accion,
        entidadTipo: f.entidadTipo,
        resultado: f.resultado,
        pagina: 0,
        tamano: MAX_EVENTOS_AUDITORIA,
      })
      .subscribe({
        next: (lista) => {
          this.eventosAuditoria.set((lista ?? []).map((e) => this.aEventoVista(e)));
          this.pagina.set(0);
          this.cargandoAuditoria.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.cargandoAuditoria.set(false);
          this.mensajeError.set(this.textoError(err, 'No se pudo consultar la auditoría.'));
        },
      });
  }

  aplicarFiltros(): void {
    const fechasInvertidas =
      this.filtroFechaDesde &&
      this.filtroFechaHasta &&
      this.filtroFechaDesde > this.filtroFechaHasta;
    if (fechasInvertidas) {
      this.errorFiltros.set('La fecha inicial no puede ser posterior a la fecha final.');
      return;
    }
    this.errorFiltros.set('');
    this.filtrosAplicados.set({
      fechaDesde: this.filtroFechaDesde,
      fechaHasta: this.filtroFechaHasta,
      correoUsuario: this.filtroCorreo,
      accion: this.filtroAccion,
      entidadTipo: this.filtroEntidad,
      resultado: this.filtroResultado,
    });
    this.cargarAuditoria();
  }

  limpiarFiltros(): void {
    this.filtroFechaDesde = '';
    this.filtroFechaHasta = '';
    this.filtroCorreo = '';
    this.filtroAccion = '';
    this.filtroEntidad = '';
    this.filtroResultado = '';
    this.errorFiltros.set('');
    this.filtrosAplicados.set({ ...FILTROS_AUDITORIA_VACIOS });
    this.cargarAuditoria();
  }

  paginaAnterior(): void {
    if (this.pagina() > 0) this.pagina.update((p) => p - 1);
  }

  paginaSiguiente(): void {
    if (this.pagina() < this.totalPaginas() - 1) this.pagina.update((p) => p + 1);
  }

  // Utilidades

  private aEventoVista(e: any): EventoAuditoriaVista {
    return {
      idAuditoria: e.idAuditoria,
      correoUsuario: e.correoUsuario ?? '',
      accion: e.accion,
      entidadTipo: e.entidadTipo,
      entidadId: e.entidadId ?? null,
      fechaHora: e.fechaHora,
      descripcion: e.descripcion ?? '',
      resultado: e.resultado,
    };
  }

  // Los errores del backend llegan como texto; si no hay, se usa el mensaje por defecto
  private textoError(err: HttpErrorResponse, porDefecto: string): string {
    if (err.status === 0) return 'No fue posible conectar con el servidor.';
    if (typeof err.error === 'string' && err.error) return err.error;
    return porDefecto;
  }
}
