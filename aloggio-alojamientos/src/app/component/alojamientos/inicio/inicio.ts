import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../service/auth.service';
import { AlojamientoService } from '../../../service/alojamiento.service';
import { AlojamientoResumenDTO } from '../../../model/alojamiento.model';
import { TipoAlojamiento } from '../../../model/publicacion.model';

@Component({
  imports: [CommonModule, FormsModule, RouterModule],
  selector: 'app-inicio.component',
  styleUrl: './inicio.scss',
  templateUrl: './inicio.html',
})
export class Inicio implements OnInit {
  private alojamientoService = inject(AlojamientoService);
  auth = inject(AuthService);

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
  readonly serviciosFiltro = ['Wi-Fi', 'Parqueadero', 'Piscina'];

  // Criterios de búsqueda
  ciudad = '';
  fechaLlegada = '';
  fechaSalida = '';
  capacidad: number | null = null;
  tipoSeleccionado: TipoAlojamiento | '' = '';
  precioMax: number | null = null;
  serviciosSeleccionados: string[] = [];

  hoy = this.fechaHoy();

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
        ciudad: this.ciudad,
        fechaLlegada: this.fechaLlegada,
        fechaSalida: this.fechaSalida,
        capacidad: this.capacidad,
        tipo: this.tipoSeleccionado,
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

  // Mismas reglas que valida el backend, para avisar antes de enviar la petición
  private validarCriterios(): string | null {
    if (!!this.fechaLlegada !== !!this.fechaSalida) {
      return 'Indica la fecha de llegada y la de salida.';
    }
    if (this.fechaLlegada && this.fechaSalida) {
      if (this.fechaLlegada < this.hoy) return 'La fecha de llegada no puede ser anterior a hoy.';
      if (this.fechaSalida <= this.fechaLlegada) {
        return 'La fecha de salida debe ser posterior a la de llegada.';
      }
    }
    if (this.capacidad !== null && this.capacidad < 1) {
      return 'La capacidad debe ser de al menos 1 huésped.';
    }
    if (this.precioMax !== null && this.precioMax < 0) {
      return 'El precio máximo no puede ser negativo.';
    }
    return null;
  }

  private mensajeDeError(err: HttpErrorResponse): string {
    if (err.status === 0) return 'No fue posible conectar con el servidor. Intenta de nuevo.';
    if (typeof err.error === 'string' && err.error) return err.error;
    return 'Ocurrió un error al buscar alojamientos. Intenta de nuevo.';
  }

  private fechaHoy(): string {
    const d = new Date();
    const mes = String(d.getMonth() + 1).padStart(2, '0');
    const dia = String(d.getDate()).padStart(2, '0');
    return `${d.getFullYear()}-${mes}-${dia}`;
  }
}
