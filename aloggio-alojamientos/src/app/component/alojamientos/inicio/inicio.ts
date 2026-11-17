import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../service/auth.service';
import { AlojamientoService } from '../../../service/alojamiento.service';
import { NotificacionService } from '../../../service/notificacion.service';
import { AlojamientoResumenDTO } from '../../../model/alojamiento.model';
import { TipoAlojamiento } from '../../../model/publicacion.model';
import { SERVICIOS_CATALOGO, claveTexto } from '../../../model/servicios-catalogo';
import { forkJoin, of } from 'rxjs';
import { FiltroEntrada } from '../../../directive/filtro-entrada.directive';

/** Límite razonable para el contador de huéspedes. */
const HUESPEDES_MAX = 20;
/** Páginas (de 50) que se recorren para armar la lista de ciudades. */
const PAGINAS_CIUDADES = 4;

@Component({
  imports: [CommonModule, FormsModule, RouterModule, FiltroEntrada],
  selector: 'app-inicio.component',
  styleUrl: './inicio.scss',
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  private alojamientoService = inject(AlojamientoService);
  auth = inject(AuthService);
  notificaciones = inject(NotificacionService);

  readonly tamanoPagina = 9;
  readonly tipos: { valor: TipoAlojamiento | ''; etiqueta: string }[] = [
    { valor: '', etiqueta: 'Todos' },
    { valor: 'APARTAMENTO', etiqueta: 'Apartamento' },
    { valor: 'CASA', etiqueta: 'Casa' },
    { valor: 'HABITACION', etiqueta: 'Habitación' },
    { valor: 'CABAÑA', etiqueta: 'Cabaña' },
  ];
  readonly etiquetasTipo: Record<string, string> = {
    CASA: 'Casa',
    APARTAMENTO: 'Apartamento',
    HABITACION: 'Habitación',
    CABAÑA: 'Cabaña',
  };
  readonly serviciosFiltro = SERVICIOS_CATALOGO;
  readonly huespedesMax = HUESPEDES_MAX;

  /** Ciudades con alojamientos activos, para el autocompletado. */
  ciudadesDisponibles = signal<string[]>([]);
  mostrarTodosServicios = signal(false);
  serviciosVisibles = computed(() =>
    this.mostrarTodosServicios() ? this.serviciosFiltro : this.serviciosFiltro.slice(0, 5),
  );

  // Criterios de búsqueda
  ciudad = '';
  fechaLlegada = '';
  fechaSalida = '';
  capacidad: number | null = null;
  tipoSeleccionado: TipoAlojamiento | '' = '';
  precioMin: number | null = null;
  precioMax: number | null = null;
  serviciosSeleccionados: string[] = [];

  hoy = this.fechaHoy();
  /** Búsquedas hasta dos años adelante (evita años de 5 cifras escritos a mano). */
  fechaMaxima = this.fechaEnAnios(2);

  // Resultados y estado de la pantalla
  alojamientos = signal<AlojamientoResumenDTO[]>([]);
  totalElementos = signal(0);
  pagina = signal(0);
  totalPaginas = signal(0);
  cargando = signal(false);
  error = signal<string | null>(null);
  busquedaRealizada = signal(false);

  menuAbierto = signal(false);

  ngOnInit(): void {
    this.buscar();
    this.cargarCiudades();
    // Solo para el contador del encabezado; si falla, simplemente no se muestra
    if (this.auth.isLoggedIn()) {
      this.notificaciones.obtenerMisNotificaciones().subscribe({ error: () => {} });
    }
  }

  buscar(pagina = 0): void {
    const mensaje = this.validarCriterios();
    if (mensaje) {
      this.error.set(mensaje);
      return;
    }

    this.cargando.set(true);
    this.error.set(null);

    this.alojamientoService
      .buscar({
        ciudad: this.ciudadParaBuscar(),
        fechaLlegada: this.fechaLlegada,
        fechaSalida: this.fechaSalida,
        capacidad: this.capacidad,
        tipo: this.tipoSeleccionado,
        precioMin: this.precioMin,
        precioMax: this.precioMax,
        servicios: this.serviciosSeleccionados,
        pagina,
        tamano: this.tamanoPagina,
      })
      .subscribe({
      next: (res) => {
        this.alojamientos.set(res.contenido);
        this.totalElementos.set(res.totalElementos);
        this.pagina.set(res.paginaActual);
        this.totalPaginas.set(res.totalPaginas);
        this.busquedaRealizada.set(true);
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.alojamientos.set([]);
        this.totalElementos.set(0);
        this.totalPaginas.set(0);
        this.error.set(this.mensajeDeError(err));
        this.cargando.set(false);
      },
    });
  }

  cambiarPagina(cambio: number): void {
    const nueva = this.pagina() + cambio;
    if (nueva < 0 || nueva >= this.totalPaginas()) return;
    this.buscar(nueva);
  }

  limpiarFiltros(): void {
    this.ciudad = '';
    this.fechaLlegada = '';
    this.fechaSalida = '';
    this.capacidad = null;
    this.tipoSeleccionado = '';
    this.precioMin = null;
    this.precioMax = null;
    this.serviciosSeleccionados = [];
    this.buscar();
  }

  servicioMarcado(nombre: string): boolean {
    return this.serviciosSeleccionados.includes(nombre);
  }

  alternarServicio(nombre: string): void {
    if (this.servicioMarcado(nombre)) {
      this.serviciosSeleccionados = this.serviciosSeleccionados.filter((s) => s !== nombre);
    } else {
      this.serviciosSeleccionados = [...this.serviciosSeleccionados, nombre];
    }
    this.buscar();
  }

  alternarMenu(): void {
    this.menuAbierto.set(!this.menuAbierto());
  }

  cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  cerrarSesion(): void {
    this.cerrarMenu();
    this.auth.logout();
  }

  // Huéspedes

  cambiarHuespedes(delta: number): void {
    const actual = this.capacidad ?? 0;
    const nuevo = actual + delta;
    // Bajar de 1 vuelve a "Cualquiera" (sin filtro de capacidad)
    this.capacidad = nuevo < 1 ? null : Math.min(nuevo, HUESPEDES_MAX);
  }

  // Ciudades

  /** Arma la lista de ciudades con las publicaciones activas (sin tocar el back). */
  private cargarCiudades(): void {
    this.alojamientoService.buscar({ pagina: 0, tamano: 50 }).subscribe({
      next: (primera) => {
        const extra = Math.min(primera.totalPaginas, PAGINAS_CIUDADES) - 1;
        const resto = Array.from({ length: Math.max(0, extra) }, (_, i) =>
          this.alojamientoService.buscar({ pagina: i + 1, tamano: 50 }),
        );
        (resto.length ? forkJoin(resto) : of([])).subscribe({
          next: (paginas) => {
            const todas = [primera, ...paginas].flatMap((p) => p.contenido.map((a) => a.ciudad));
            const unicas = new Map<string, string>();
            for (const ciudad of todas) {
              if (ciudad && !unicas.has(claveTexto(ciudad))) unicas.set(claveTexto(ciudad), ciudad.trim());
            }
            this.ciudadesDisponibles.set([...unicas.values()].sort((a, b) => a.localeCompare(b, 'es')));
          },
          error: () => {},
        });
      },
      // Sin lista de ciudades el campo sigue funcionando como texto libre
      error: () => {},
    });
  }

  /**
   * El back compara la ciudad exacta (sin mayúsculas). Si lo escrito coincide con una
   * ciudad conocida ignorando tildes ("medellin"), se envía con su escritura real.
   */
  private ciudadParaBuscar(): string {
    const texto = this.ciudad.trim().replace(/\s+/g, ' ');
    if (!texto) return '';
    const conocida = this.ciudadesDisponibles().find((c) => claveTexto(c) === claveTexto(texto));
    return conocida ?? texto;
  }

  // Precios

  /** null si está vacío, false si es inválido, o el número. */
  private precioValido(valor: number | null): number | null | false {
    if (valor === null || (valor as unknown) === '') return null;
    const n = Number(valor);
    return Number.isFinite(n) && n >= 0 ? n : false;
  }

  // Mismas reglas que valida el backend, para avisar antes de enviar la petición
  private validarCriterios(): string | null {
    if (!!this.fechaLlegada !== !!this.fechaSalida) {
      return 'Indica la fecha de llegada y la de salida.';
    }
    if (this.fechaLlegada && this.fechaSalida) {
      if (this.fechaLlegada < this.hoy) return 'La fecha de llegada no puede ser anterior a hoy.';
      if (this.fechaLlegada > this.fechaMaxima || this.fechaSalida > this.fechaMaxima) {
        return 'Las fechas pueden estar como máximo dentro de dos años.';
      }
      if (this.fechaSalida <= this.fechaLlegada) {
        return 'La fecha de salida debe ser posterior a la de llegada.';
      }
    }
    if (this.capacidad !== null) {
      const c = Number(this.capacidad);
      if (!Number.isInteger(c) || c < 1 || c > HUESPEDES_MAX) {
        return `Los huéspedes deben ser un número entero entre 1 y ${HUESPEDES_MAX}.`;
      }
    }
    const min = this.precioValido(this.precioMin);
    const max = this.precioValido(this.precioMax);
    if (min === false || max === false) return 'Los precios deben ser números de cero o mayores.';
    if (min !== null && max !== null && min > max) {
      return 'El precio mínimo no puede ser mayor que el máximo.';
    }
    return null;
  }

  private mensajeDeError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No fue posible conectar con el servidor. Intenta de nuevo.';
    if (typeof err.error === 'string' && err.error) return err.error;
    return 'Ocurrió un error al buscar alojamientos. Intenta de nuevo.';
  }

  private fechaEnAnios(anios: number): string {
    const d = new Date();
    const f = new Date(d.getFullYear() + anios, d.getMonth(), d.getDate());
    return `${f.getFullYear()}-${String(f.getMonth() + 1).padStart(2, '0')}-${String(f.getDate()).padStart(2, '0')}`;
  }

  private fechaHoy(): string {
    const d = new Date();
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mes}-${dia}`;
  }
}
