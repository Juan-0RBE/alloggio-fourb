import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { NotificacionService } from '../../../service/notificacion.service';
import { NotificacionDTO, TipoNotificacion } from '../../../model/notificacion.model';

type Filtro = 'TODAS' | 'NO_LEIDAS';

export type NotificacionVista = NotificacionDTO;

export const ETIQUETAS_TIPO: Record<TipoNotificacion, string> = {
  RESERVA: 'Reserva',
  PAGO: 'Pago',
  SISTEMA: 'Sistema',
  ALERTA: 'Alerta',
};

export const ICONOS_TIPO: Record<TipoNotificacion, string> = {
  RESERVA: 'pi-calendar',
  PAGO: 'pi-credit-card',
  SISTEMA: 'pi-cog',
  ALERTA: 'pi-exclamation-triangle',
};

@Component({
  selector: 'app-bandeja-notificaciones',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './bandeja-notificaciones.html',
  styleUrl: './bandeja-notificaciones.scss',
})
export class BandejaNotificaciones implements OnInit {
  private notificacionService = inject(NotificacionService);

  readonly filtros: { valor: Filtro; etiqueta: string }[] = [
    { valor: 'TODAS', etiqueta: 'Todas' },
    { valor: 'NO_LEIDAS', etiqueta: 'Sin leer' },
  ];
  readonly etiquetasTipo = ETIQUETAS_TIPO;
  readonly iconosTipo = ICONOS_TIPO;

  filtroActivo = signal<Filtro>('TODAS');
  cargando = signal(false);
  error = signal<{ mensaje: string; sesion: boolean } | null>(null);

  lista = signal<NotificacionVista[]>([]);
  /** Ids que se están marcando como leídas, para no repetir la petición. */
  private marcando = new Set<number>();
  errorAccion = signal('');

  ordenadas = computed(() =>
    [...this.lista()].sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime()),
  );

  visibles = computed(() =>
    this.filtroActivo() === 'NO_LEIDAS'
      ? this.ordenadas().filter((n) => !n.estaLeida)
      : this.ordenadas(),
  );

  noLeidas = computed(() => this.lista().filter((n) => !n.estaLeida).length);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.error.set(null);
    this.cargando.set(true);
    this.notificacionService.obtenerMisNotificaciones().subscribe({
      next: (lista) => {
        this.lista.set(lista.filter((n) => n.idNotificacion != null));
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        if (err.status === 401 || err.status === 403) {
          this.error.set({ mensaje: 'Tu sesión expiró. Inicia sesión de nuevo.', sesion: true });
        } else if (err.status === 0) {
          this.error.set({ mensaje: 'No hay conexión con el servidor.', sesion: false });
        } else {
          this.error.set({ mensaje: 'No pudimos cargar tus notificaciones.', sesion: false });
        }
      },
    });
  }

  seleccionarFiltro(filtro: Filtro): void {
    this.filtroActivo.set(filtro);
  }

  /** Se marca en pantalla de inmediato y se revierte si el back falla. */
  marcarComoLeida(notificacion: NotificacionVista): void {
    const id = notificacion.idNotificacion;
    if (notificacion.estaLeida || this.marcando.has(id)) return;
    this.marcando.add(id);
    this.errorAccion.set('');
    this.cambiarLeida(id, true);

    this.notificacionService.marcarComoLeida(id).subscribe({
      next: () => this.marcando.delete(id),
      error: () => {
        this.marcando.delete(id);
        this.cambiarLeida(id, false);
        this.errorAccion.set('No se pudo marcar la notificación como leída. Intenta de nuevo.');
      },
    });
  }

  marcarTodasComoLeidas(): void {
    for (const n of this.lista()) {
      if (!n.estaLeida) this.marcarComoLeida(n);
    }
  }

  private cambiarLeida(id: number, leida: boolean): void {
    this.lista.update((lista) =>
      lista.map((n) => (n.idNotificacion === id ? { ...n, estaLeida: leida } : n)),
    );
  }
}
