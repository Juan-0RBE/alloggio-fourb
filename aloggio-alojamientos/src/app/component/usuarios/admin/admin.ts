import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

type SeccionAdmin =
  'dashboard' | 'reportes' | 'publicaciones' | 'resenas' | 'usuarios' | 'auditoria';

interface Reporte {
  idReporte: number;
  tipo: 'Reservas e Ingresos' | 'Actividad de Usuarios' | 'Publicaciones Activas';
  formato: 'CSV' | 'JSON';
  solicitadoPor: string;
  fechaSolicitud: string;
}

interface PublicacionMod {
  idPublicacion: number;
  titulo: string;
  anfitrion: string;
  ciudad: string;
  precioNoche: number;
  estado: 'ACTIVA' | 'PAUSADA' | 'RETIRADA';
}

interface ResenaMod {
  idResena: number;
  alojamiento: string;
  autor: string;
  calificacion: number;
  comentario: string;
  fecha: string;
  visible: boolean;
}

interface UsuarioMod {
  idUsuario: number;
  nombre: string;
  correo: string;
  rol: 'HUESPED' | 'ANFITRION' | 'ADMIN';
  restringido: boolean;
}

interface EventoAuditoriaMER {
  idEvento: number;
  fecha: string;
  usuario: string;
  accion: string;
  entidadAfectada: string;
  resultado: 'EXITO' | 'DENEGADO';
}

@Component({
  selector: 'app-panel-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin.html',
  styleUrl: './admin.scss',
})
export class Admin {
  readonly adminActual = 'admin@aloggio.co';
  seccionActiva = signal<SeccionAdmin>('dashboard');

  tipoReporteSeleccionado: Reporte['tipo'] = 'Reservas e Ingresos';
  formatoReporteSeleccionado: Reporte['formato'] = 'CSV';
  historialReportes = signal<Reporte[]>([
    {
      idReporte: 501,
      tipo: 'Reservas e Ingresos',
      formato: 'CSV',
      solicitadoPor: 'admin@aloggio.co',
      fechaSolicitud: '2026-11-12 14:30',
    },
  ]);

  publicaciones = signal<PublicacionMod[]>([
    {
      idPublicacion: 1,
      titulo: 'Villa Moderna Vista al Valle y Montaña',
      anfitrion: 'Carlos Mendoza',
      ciudad: 'Medellín',
      precioNoche: 550000,
      estado: 'ACTIVA',
    },
    {
      idPublicacion: 2,
      titulo: 'Cabaña Alpina en el Bosque',
      anfitrion: 'Laura Gómez',
      ciudad: 'Guatapé',
      precioNoche: 350000,
      estado: 'ACTIVA',
    },
    {
      idPublicacion: 3,
      titulo: 'Apartamento con fiesta permitida 24/7 (Reportado)',
      anfitrion: ' Andrés Rojas',
      ciudad: 'Cartagena',
      precioNoche: 290000,
      estado: 'PAUSADA',
    },
  ]);

  resenas = signal<ResenaMod[]>([
    {
      idResena: 88,
      alojamiento: 'Villa Moderna Vista al Valle y Montaña',
      autor: 'Sofía Martínez',
      calificacion: 5,
      comentario: 'Excelente estadía, la vista es espectacular y todo estaba impecable.',
      fecha: '2026-11-05',
      visible: true,
    },
    {
      idResena: 89,
      alojamiento: 'Cabaña Alpina en el Bosque',
      autor: 'usuario_spam99',
      calificacion: 1,
      comentario: 'Visiten mi página externa para descuentos falsos http://spam-link.test',
      fecha: '2026-11-10',
      visible: false,
    },
  ]);

  busquedaUsuario = signal('');
  usuarios = signal<UsuarioMod[]>([
    {
      idUsuario: 101,
      nombre: 'Carlos Mendoza',
      correo: 'carlos@correo.com',
      rol: 'ANFITRION',
      restringido: false,
    },
    {
      idUsuario: 102,
      nombre: 'Sofía Martínez',
      correo: 'sofia@correo.com',
      rol: 'HUESPED',
      restringido: false,
    },
    {
      idUsuario: 103,
      nombre: 'Andrés Rojas',
      correo: 'andres.infractor@correo.com',
      rol: 'ANFITRION',
      restringido: true,
    },
    {
      idUsuario: 104,
      nombre: 'Laura Gómez',
      correo: 'laura@correo.com',
      rol: 'ANFITRION',
      restringido: false,
    },
  ]);

  usuariosFiltrados = computed(() => {
    const termino = this.busquedaUsuario().trim().toLowerCase();
    if (!termino) return this.usuarios();
    return this.usuarios().filter(
      (u) =>
        u.nombre.toLowerCase().includes(termino) ||
        u.correo.toLowerCase().includes(termino) ||
        String(u.idUsuario).includes(termino),
    );
  });

  filtroAccionAuditoria = signal('');
  filtroResultadoAuditoria = signal<'TODOS' | 'EXITO' | 'DENEGADO'>('TODOS');

  eventosAuditoria = signal<EventoAuditoriaMER[]>([
    {
      idEvento: 1001,
      fecha: '2026-11-10 09:15',
      usuario: 'admin@aloggio.co',
      accion: 'OCULTAR_RESENA',
      entidadAfectada: 'RESENA #89',
      resultado: 'EXITO',
    },
    {
      idEvento: 1002,
      fecha: '2026-11-11 16:40',
      usuario: 'admin@aloggio.co',
      accion: 'RESTRINGIR_USUARIO',
      entidadAfectada: 'USUARIO #103',
      resultado: 'EXITO',
    },
  ]);

  eventosFiltrados = computed(() => {
    const texto = this.filtroAccionAuditoria().trim().toLowerCase();
    const res = this.filtroResultadoAuditoria();
    return this.eventosAuditoria().filter((ev) => {
      const coincideTexto =
        !texto ||
        ev.accion.toLowerCase().includes(texto) ||
        ev.entidadAfectada.toLowerCase().includes(texto) ||
        ev.usuario.toLowerCase().includes(texto);
      const coincideRes = res === 'TODOS' || ev.resultado === res;
      return coincideTexto && coincideRes;
    });
  });

  indicadores = computed(() => ({
    totalUsuarios: this.usuarios().length,
    usuariosRestringidos: this.usuarios().filter((u) => u.restringido).length,
    publicacionesActivas: this.publicaciones().filter((p) => p.estado === 'ACTIVA').length,
    totalPublicaciones: this.publicaciones().length,
    resenasOcultas: this.resenas().filter((r) => !r.visible).length,
    totalResenas: this.resenas().length,
    totalEventos: this.eventosAuditoria().length,
  }));

  cambiarSeccion(seccion: SeccionAdmin): void {
    this.seccionActiva.set(seccion);
  }

  generarReporte(): void {
    const ahora = this.fechaActualFormateada();
    const nuevoReporte: Reporte = {
      idReporte: Math.floor(500 + Math.random() * 499),
      tipo: this.tipoReporteSeleccionado,
      formato: this.formatoReporteSeleccionado,
      solicitadoPor: this.adminActual,
      fechaSolicitud: ahora,
    };

    this.historialReportes.update((lista) => [nuevoReporte, ...lista]);
    this.descargarArchivoSimulado(nuevoReporte);
    this.registrarAuditoria(
      'GENERAR_REPORTE',
      `REPORTE #${nuevoReporte.idReporte} (${nuevoReporte.tipo} - ${nuevoReporte.formato})`,
      'EXITO',
    );
  }

  cambiarEstadoPublicacion(pub: PublicacionMod, nuevoEstado: PublicacionMod['estado']): void {
    if (pub.estado === nuevoEstado) return;
    this.publicaciones.update((lista) =>
      lista.map((p) => (p.idPublicacion === pub.idPublicacion ? { ...p, estado: nuevoEstado } : p)),
    );
    this.registrarAuditoria(
      `PUBLICACION_${nuevoEstado}`,
      `PUBLICACION #${pub.idPublicacion}`,
      'EXITO',
    );
  }

  alternarVisibilidadResena(resena: ResenaMod): void {
    const nuevoVisible = !resena.visible;
    this.resenas.update((lista) =>
      lista.map((r) => (r.idResena === resena.idResena ? { ...r, visible: nuevoVisible } : r)),
    );
    const accion = nuevoVisible ? 'MOSTRAR_RESENA' : 'OCULTAR_RESENA';
    this.registrarAuditoria(accion, `RESENA #${resena.idResena}`, 'EXITO');
  }

  alternarRestriccionUsuario(usuario: UsuarioMod): void {
    if (usuario.rol === 'ADMIN') {
      this.registrarAuditoria('RESTRINGIR_USUARIO', `USUARIO #${usuario.idUsuario}`, 'DENEGADO');
      return;
    }
    const nuevoEstado = !usuario.restringido;
    this.usuarios.update((lista) =>
      lista.map((u) =>
        u.idUsuario === usuario.idUsuario ? { ...u, restringido: nuevoEstado } : u,
      ),
    );
    const accion = nuevoEstado ? 'RESTRINGIR_USUARIO' : 'LEVANTAR_RESTRICCION_USUARIO';
    this.registrarAuditoria(accion, `USUARIO #${usuario.idUsuario} (${usuario.correo})`, 'EXITO');
  }

  limpiarDatosPrueba(): void {
    this.publicaciones.set([]);
    this.resenas.set([]);
    this.usuarios.set([]);
    this.eventosAuditoria.set([]);
    this.historialReportes.set([]);
  }

  private registrarAuditoria(
    accion: string,
    entidadAfectada: string,
    resultado: 'EXITO' | 'DENEGADO',
  ): void {
    const nuevoEvento: EventoAuditoriaMER = {
      idEvento: Date.now() % 100000,
      fecha: this.fechaActualFormateada(),
      usuario: this.adminActual,
      accion,
      entidadAfectada,
      resultado,
    };
    this.eventosAuditoria.update((lista) => [nuevoEvento, ...lista]);
  }

  private descargarArchivoSimulado(reporte: Reporte): void {
    let contenido = '';
    let mime = 'text/plain;charset=utf-8';

    if (reporte.formato === 'JSON') {
      contenido = JSON.stringify(
        {
          meta: reporte,
          indicadores: this.indicadores(),
          publicaciones: this.publicaciones(),
          usuarios: this.usuarios(),
        },
        null,
        2,
      );
      mime = 'application/json;charset=utf-8';
    } else {
      contenido = `ID_REPORTE,TIPO,SOLICITADO_POR,FECHA_SOLICITUD,PUBLICACIONES_ACTIVAS,USUARIOS_TOTALES\n`;
      contenido += `${reporte.idReporte},"${reporte.tipo}",${reporte.solicitadoPor},${reporte.fechaSolicitud},${this.indicadores().publicacionesActivas},${this.indicadores().totalUsuarios}`;
      mime = 'text/csv;charset=utf-8';
    }

    const blob = new Blob([contenido], { type: mime });
    const url = URL.createObjectURL(blob);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = `aloggio-reporte-${reporte.idReporte}.${reporte.formato.toLowerCase()}`;
    enlace.click();
    URL.revokeObjectURL(url);
  }

  private fechaActualFormateada(): string {
    const d = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }
}
