import { Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';

type Filtro = 'TODAS' | 'NO_LEIDAS';
type TipoNotificacion = 'RESERVA' | 'PAGO' | 'SISTEMA' | 'ALERTA';

export interface NotificacionVista {
  idNotificacion: number;
  tipoNotificacion: TipoNotificacion;
  mensaje: string;
  fecha: string;
  estaLeida: boolean;
}

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
  readonly filtros: { valor: Filtro; etiqueta: string }[] = [
    { valor: 'TODAS', etiqueta: 'Todas' },
    { valor: 'NO_LEIDAS', etiqueta: 'Sin leer' },
  ];
  readonly etiquetasTipo = ETIQUETAS_TIPO;
  readonly iconosTipo = ICONOS_TIPO;

  filtroActivo = signal<Filtro>('TODAS');
  cargando = signal(false);
  error = signal<any | null>(null);

  lista = signal<NotificacionVista[]>([
    {
      idNotificacion: 305,
      tipoNotificacion: 'RESERVA',
      mensaje: 'Tu reserva #1042 en Villa Moderna Vista al Valle y Montaña fue confirmada.',
      fecha: '2026-11-14T16:20:00',
      estaLeida: false,
    },
    {
      idNotificacion: 304,
      tipoNotificacion: 'PAGO',
      mensaje: 'Recibimos tu pago de $2.835.000 (modo de pruebas).',
      fecha: '2026-11-14T16:19:00',
      estaLeida: false,
    },
    {
      idNotificacion: 290,
      tipoNotificacion: 'ALERTA',
      mensaje: 'Tu publicación "Apartamento con fiesta permitida 24/7" fue bloqueada.',
      fecha: '2026-11-11T09:05:00',
      estaLeida: true,
    },
    {
      idNotificacion: 281,
      tipoNotificacion: 'SISTEMA',
      mensaje: 'Bienvenido a Aloggio. Ya puedes explorar alojamientos.',
      fecha: '2026-11-01T10:00:00',
      estaLeida: true,
    },
  ]);

  ordenadas = computed(() =>
    [...this.lista()].sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime()),
  );

  visibles = computed(() =>
    this.filtroActivo() === 'NO_LEIDAS'
      ? this.ordenadas().filter((n) => !n.estaLeida)
      : this.ordenadas(),
  );

  noLeidas = computed(() => this.lista().filter((n) => !n.estaLeida).length);

  ngOnInit(): void {}

  cargar(): void {
    this.cargando.set(true);
    setTimeout(() => {
      this.cargando.set(false);
    }, 400);
  }

  seleccionarFiltro(filtro: Filtro): void {
    this.filtroActivo.set(filtro);
  }

  marcarComoLeida(notificacion: NotificacionVista): void {
    if (notificacion.estaLeida) return;
    this.lista.update((lista) =>
      lista.map((n) =>
        n.idNotificacion === notificacion.idNotificacion ? { ...n, estaLeida: true } : n,
      ),
    );
  }
}
