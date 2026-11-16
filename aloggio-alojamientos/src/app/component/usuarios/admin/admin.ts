import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

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
export class Admin {
  readonly adminActual = 'admin@aloggio.co';
  readonly idAdminActual = 100;
  readonly longitudMaxMotivo = LONGITUD_MAX_MOTIVO;
  readonly accionesAuditoria = ACCIONES_AUDITORIA;
  readonly tiposEntidad = TIPOS_ENTIDAD;

  seccionActiva = signal<SeccionAdmin>('dashboard');
  mensajeConfirmacion = signal('');

  // Dashboard y exportación (CA51, CA52, CA53)

  dashboard = signal<DashboardVista>({
    totalUsuarios: 5,
    usuariosPorEstadoCuenta: { ACTIVA: 4, RESTRINGIDA: 1 },
    totalPublicaciones: 4,
    publicacionesActivas: 2,
    publicacionesPorEstado: { ACTIVA: 2, PAUSADA: 1, BLOQUEADA: 1 },
    totalReservas: 6,
    reservasPorEstado: { CONFIRMADA: 3, COMPLETADA: 2, CANCELADA: 1 },
    ingresosEstimados: 8450000,
    ciudadesDemanda: [
      { ciudad: 'Medellín', reservas: 3, ingresos: 4200000 },
      { ciudad: 'Cartagena', reservas: 2, ingresos: 2900000 },
      { ciudad: 'Guatapé', reservas: 1, ingresos: 1350000 },
    ],
    sinDatos: false,
  });

  reservasPorEstado = computed(() => Object.entries(this.dashboard().reservasPorEstado));

  formatoExportacion: FormatoExportacion = 'PDF';

  exportaciones = computed(() =>
    this.eventosOrdenados().filter((ev) => ev.accion === 'EXPORTAR_INDICADORES'),
  );

  // Moderación de publicaciones (CA46, CA47)

  filtroEstadoPublicacion = signal<'TODAS' | EstadoPublicacion>('TODAS');
  publicacionSeleccionada = signal<PublicacionMod | null>(null);
  errorBloqueo = signal('');
  motivoBloqueo = '';

  publicaciones = signal<PublicacionMod[]>([
    {
      idPublicacion: 1,
      titulo: 'Villa Moderna Vista al Valle y Montaña',
      ciudad: 'Medellín',
      precioNoche: 550000,
      estado: 'ACTIVA',
      motivoBloqueo: null,
      nombreAnfitrion: 'Carlos Mendoza',
      totalReservasFuturas: 1,
      reservasFuturas: [
        {
          idReserva: 1042,
          fechaLlegada: '2026-12-10',
          fechaSalida: '2026-12-15',
          total: 2835000,
          nombreHuesped: 'Sofía Martínez',
        },
      ],
    },
    {
      idPublicacion: 2,
      titulo: 'Cabaña Alpina en el Bosque',
      ciudad: 'Guatapé',
      precioNoche: 350000,
      estado: 'ACTIVA',
      motivoBloqueo: null,
      nombreAnfitrion: 'Laura Gómez',
      totalReservasFuturas: 0,
      reservasFuturas: [],
    },
    {
      idPublicacion: 3,
      titulo: 'Apartamento con fiesta permitida 24/7',
      ciudad: 'Cartagena',
      precioNoche: 290000,
      estado: 'PAUSADA',
      motivoBloqueo: null,
      nombreAnfitrion: 'Andrés Rojas',
      totalReservasFuturas: 2,
      reservasFuturas: [
        {
          idReserva: 1050,
          fechaLlegada: '2026-12-20',
          fechaSalida: '2026-12-23',
          total: 1015000,
          nombreHuesped: 'Sofía Martínez',
        },
        {
          idReserva: 1058,
          fechaLlegada: '2027-01-04',
          fechaSalida: '2027-01-06',
          total: 700000,
          nombreHuesped: 'Camilo Duarte',
        },
      ],
    },
    {
      idPublicacion: 4,
      titulo: 'Habitación en el centro histórico',
      ciudad: 'Cartagena',
      precioNoche: 180000,
      estado: 'BLOQUEADA',
      motivoBloqueo: 'Las fotos no corresponden al alojamiento publicado.',
      nombreAnfitrion: 'Andrés Rojas',
      totalReservasFuturas: 0,
      reservasFuturas: [],
    },
  ]);

  publicacionesFiltradas = computed(() => {
    const estado = this.filtroEstadoPublicacion();
    if (estado === 'TODAS') return this.publicaciones();
    return this.publicaciones().filter((p) => p.estado === estado);
  });

  // Moderación de reseñas (CA45)

  alojamientoSeleccionado = signal<number | null>(1);
  resenaEnConfirmacion = signal<number | null>(null);

  resenas = signal<ResenaMod[]>([
    {
      idResena: 88,
      idPublicacion: 1,
      autor: 'sofia.martinez',
      calificacion: 5,
      comentario: 'Excelente estadía, la vista es espectacular y todo estaba impecable.',
      fecha: '2026-11-05',
      visible: true,
    },
    {
      idResena: 89,
      idPublicacion: 1,
      autor: 'usuario_spam99',
      calificacion: 1,
      comentario: 'Visiten mi página externa para descuentos falsos http://spam-link.test',
      fecha: '2026-11-10',
      visible: false,
    },
    {
      idResena: 90,
      idPublicacion: 2,
      autor: 'camilo.duarte',
      calificacion: 4,
      comentario: 'Muy tranquila, ideal para descansar. El camino de llegada es algo largo.',
      fecha: '2026-11-08',
      visible: true,
    },
  ]);

  resenasDelAlojamiento = computed(() =>
    this.resenas().filter((r) => r.idPublicacion === this.alojamientoSeleccionado()),
  );

  // Gestión de usuarios (CA48)

  busquedaUsuario = signal('');
  filtroEstadoCuenta = signal<'TODOS' | EstadoCuenta>('TODOS');
  usuarioEnConfirmacion = signal<number | null>(null);

  usuarios = signal<UsuarioMod[]>([
    {
      idUsuario: 100,
      nombreUsuario: 'admin',
      primerNombre: 'Administrador',
      primerApellido: 'Aloggio',
      role: 'ADMINISTRADOR',
      estadoCuenta: 'ACTIVA',
    },
    {
      idUsuario: 101,
      nombreUsuario: 'carlos.mendoza',
      primerNombre: 'Carlos',
      primerApellido: 'Mendoza',
      role: 'USUARIO',
      estadoCuenta: 'ACTIVA',
    },
    {
      idUsuario: 102,
      nombreUsuario: 'sofia.martinez',
      primerNombre: 'Sofía',
      primerApellido: 'Martínez',
      role: 'USUARIO',
      estadoCuenta: 'ACTIVA',
    },
    {
      idUsuario: 103,
      nombreUsuario: 'andres.rojas',
      primerNombre: 'Andrés',
      primerApellido: 'Rojas',
      role: 'USUARIO',
      estadoCuenta: 'RESTRINGIDA',
    },
    {
      idUsuario: 104,
      nombreUsuario: 'laura.gomez',
      primerNombre: 'Laura',
      primerApellido: 'Gómez',
      role: 'USUARIO',
      estadoCuenta: 'ACTIVA',
    },
  ]);

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

  eventosAuditoria = signal<EventoAuditoriaVista[]>([
    {
      idAuditoria: 1001,
      correoUsuario: 'admin@aloggio.co',
      accion: 'OCULTAR_RESENA',
      entidadTipo: 'RESENA',
      entidadId: 89,
      fechaHora: '2026-11-10T09:15:00',
      descripcion: 'Reseña ocultada por incumplir las normas',
      resultado: 'EXITO',
    },
    {
      idAuditoria: 1002,
      correoUsuario: 'admin@aloggio.co',
      accion: 'RESTRINGIR_USUARIO',
      entidadTipo: 'USUARIO',
      entidadId: 103,
      fechaHora: '2026-11-11T16:40:00',
      descripcion: 'Cuenta restringida por un administrador',
      resultado: 'EXITO',
    },
    {
      idAuditoria: 1003,
      correoUsuario: 'admin@aloggio.co',
      accion: 'BLOQUEAR_PUBLICACION',
      entidadTipo: 'PUBLICACION',
      entidadId: 4,
      fechaHora: '2026-11-11T17:05:00',
      descripcion: 'Publicación bloqueada: las fotos no corresponden al alojamiento',
      resultado: 'EXITO',
    },
    {
      idAuditoria: 1004,
      correoUsuario: 'admin@aloggio.co',
      accion: 'EXPORTAR_INDICADORES',
      entidadTipo: 'REPORTE',
      entidadId: null,
      fechaHora: '2026-11-12T14:30:00',
      descripcion: 'Exportación de indicadores en PDF',
      resultado: 'EXITO',
    },
    {
      idAuditoria: 1005,
      correoUsuario: 'admin@aloggio.co',
      accion: 'RESTRINGIR_USUARIO',
      entidadTipo: 'USUARIO',
      entidadId: 100,
      fechaHora: '2026-11-13T10:20:00',
      descripcion: 'Intento de restringir a un administrador',
      resultado: 'DENEGADO',
    },
  ]);

  eventosOrdenados = computed(() =>
    [...this.eventosAuditoria()].sort((a, b) => b.fechaHora.localeCompare(a.fechaHora)),
  );

  eventosFiltrados = computed(() => {
    const f = this.filtrosAplicados();
    const correo = f.correoUsuario.trim().toLowerCase();
    return this.eventosOrdenados().filter(
      (ev) =>
        (!f.fechaDesde || ev.fechaHora.slice(0, 10) >= f.fechaDesde) &&
        (!f.fechaHasta || ev.fechaHora.slice(0, 10) <= f.fechaHasta) &&
        (!correo || ev.correoUsuario.toLowerCase().includes(correo)) &&
        (!f.accion || ev.accion === f.accion) &&
        (!f.entidadTipo || ev.entidadTipo === f.entidadTipo) &&
        (!f.resultado || ev.resultado === f.resultado),
    );
  });

  totalPaginas = computed(() =>
    Math.max(1, Math.ceil(this.eventosFiltrados().length / TAMANO_PAGINA)),
  );

  eventosPagina = computed(() => {
    const inicio = this.pagina() * TAMANO_PAGINA;
    return this.eventosFiltrados().slice(inicio, inicio + TAMANO_PAGINA);
  });

  cambiarSeccion(seccion: SeccionAdmin): void {
    this.mensajeConfirmacion.set('');
    this.seccionActiva.set(seccion);
  }

  // Dashboard y exportación

  simularDashboardVacio(): void {
    this.dashboard.set({ ...DASHBOARD_VACIO });
  }

  exportarIndicadores(formato: FormatoExportacion): void {
    this.registrarAuditoria(
      'EXPORTAR_INDICADORES',
      'REPORTE',
      null,
      `Exportación de indicadores en ${formato}`,
      'EXITO',
    );
    this.mensajeConfirmacion.set(
      `Exportación en ${formato} solicitada. Quedó registrada en el log de auditoría.`,
    );
  }

  // Publicaciones

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
    if (!pub || !this.puedeBloquearse(pub)) return;

    const motivo = this.motivoBloqueo.trim();
    if (motivo.length === 0 || motivo.length > LONGITUD_MAX_MOTIVO) {
      this.errorBloqueo.set(
        `El motivo es obligatorio y admite máximo ${LONGITUD_MAX_MOTIVO} caracteres.`,
      );
      return;
    }

    this.publicaciones.update((lista) =>
      lista.map((p) =>
        p.idPublicacion === pub.idPublicacion
          ? { ...p, estado: 'BLOQUEADA' as EstadoPublicacion, motivoBloqueo: motivo }
          : p,
      ),
    );
    this.registrarAuditoria(
      'BLOQUEAR_PUBLICACION',
      'PUBLICACION',
      pub.idPublicacion,
      `Publicación bloqueada: ${motivo}`,
      'EXITO',
    );
    this.publicacionSeleccionada.set(null);
    this.mensajeConfirmacion.set(
      `Publicación bloqueada. Reservas futuras conservadas para revisión manual: ${pub.totalReservasFuturas}.`,
    );
  }

  // Reseñas

  seleccionarAlojamiento(idPublicacion: number): void {
    this.resenaEnConfirmacion.set(null);
    this.alojamientoSeleccionado.set(idPublicacion);
  }

  pedirConfirmacionOcultar(resena: ResenaMod): void {
    this.resenaEnConfirmacion.set(resena.idResena);
  }

  cancelarOcultar(): void {
    this.resenaEnConfirmacion.set(null);
  }

  ocultarResena(resena: ResenaMod): void {
    if (!resena.visible) return;
    this.resenas.update((lista) =>
      lista.map((r) => (r.idResena === resena.idResena ? { ...r, visible: false } : r)),
    );
    this.registrarAuditoria(
      'OCULTAR_RESENA',
      'RESENA',
      resena.idResena,
      'Reseña ocultada por incumplir las normas',
      'EXITO',
    );
    this.resenaEnConfirmacion.set(null);
    this.mensajeConfirmacion.set(
      'Reseña ocultada. Ya no se muestra en el detalle del alojamiento.',
    );
  }

  // Usuarios

  seleccionarFiltroEstadoCuenta(estado: 'TODOS' | EstadoCuenta): void {
    this.filtroEstadoCuenta.set(estado);
  }

  puedeRestringirse(usuario: UsuarioMod): boolean {
    return (
      usuario.role !== 'ADMINISTRADOR' &&
      usuario.idUsuario !== this.idAdminActual &&
      usuario.estadoCuenta !== 'RESTRINGIDA'
    );
  }

  pedirConfirmacionRestriccion(usuario: UsuarioMod): void {
    this.usuarioEnConfirmacion.set(usuario.idUsuario);
  }

  cancelarRestriccion(): void {
    this.usuarioEnConfirmacion.set(null);
  }

  restringirUsuario(usuario: UsuarioMod): void {
    if (!this.puedeRestringirse(usuario)) return;
    this.usuarios.update((lista) =>
      lista.map((u) =>
        u.idUsuario === usuario.idUsuario
          ? { ...u, estadoCuenta: 'RESTRINGIDA' as EstadoCuenta }
          : u,
      ),
    );
    this.registrarAuditoria(
      'RESTRINGIR_USUARIO',
      'USUARIO',
      usuario.idUsuario,
      `Cuenta restringida: ${usuario.nombreUsuario}`,
      'EXITO',
    );
    this.usuarioEnConfirmacion.set(null);
    this.mensajeConfirmacion.set(
      'Cuenta restringida. El usuario ya no puede iniciar sesión ni realizar nuevas acciones.',
    );
  }

  // Auditoría

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
    this.pagina.set(0);
    this.filtrosAplicados.set({
      fechaDesde: this.filtroFechaDesde,
      fechaHasta: this.filtroFechaHasta,
      correoUsuario: this.filtroCorreo,
      accion: this.filtroAccion,
      entidadTipo: this.filtroEntidad,
      resultado: this.filtroResultado,
    });
  }

  limpiarFiltros(): void {
    this.filtroFechaDesde = '';
    this.filtroFechaHasta = '';
    this.filtroCorreo = '';
    this.filtroAccion = '';
    this.filtroEntidad = '';
    this.filtroResultado = '';
    this.errorFiltros.set('');
    this.pagina.set(0);
    this.filtrosAplicados.set({ ...FILTROS_AUDITORIA_VACIOS });
  }

  paginaAnterior(): void {
    if (this.pagina() > 0) this.pagina.update((p) => p - 1);
  }

  paginaSiguiente(): void {
    if (this.pagina() < this.totalPaginas() - 1) this.pagina.update((p) => p + 1);
  }

  private registrarAuditoria(
    accion: string,
    entidadTipo: string,
    entidadId: number | null,
    descripcion: string,
    resultado: ResultadoAuditoria,
  ): void {
    const nuevoEvento: EventoAuditoriaVista = {
      idAuditoria: Date.now() % 100000,
      correoUsuario: this.adminActual,
      accion,
      entidadTipo,
      entidadId,
      fechaHora: this.fechaHoraActual(),
      descripcion,
      resultado,
    };
    this.eventosAuditoria.update((lista) => [nuevoEvento, ...lista]);
  }

  private fechaHoraActual(): string {
    const d = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
  }
}
