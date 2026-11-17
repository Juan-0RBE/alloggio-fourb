import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ReservaService } from '../../../service/reserva.service';
import { AlojamientoService } from '../../../service/alojamiento.service';
import { ReservaDTO } from '../../../model/reserva.model';
import { AlojamientoDetalleDTO } from '../../../model/alojamiento.model';

const TIPOS_VALIDOS: TipoResultado[] = ['exito', 'rechazado', 'no-disponible', 'pasarela', 'conflicto'];

type TipoResultado = 'exito' | 'rechazado' | 'no-disponible' | 'pasarela' | 'conflicto';

interface ConfigResultado {
  icono: string;
  tono: 'exito' | 'error' | 'aviso';
  titulo: string;
  descripcion: string;
}

const RESULTADOS: Record<TipoResultado, ConfigResultado> = {
  exito: {
    icono: 'pi-check-circle',
    tono: 'exito',
    titulo: '¡Tu reserva está confirmada!',
    descripcion: 'Guarda tu código de reserva. Disfruta de tu estancia en Aloggio.',
  },
  rechazado: {
    icono: 'pi-times-circle',
    tono: 'error',
    titulo: 'No pudimos procesar tu pago',
    descripcion: 'No se hizo ningún cobro. Puedes intentarlo con otra tarjeta.',
  },
  'no-disponible': {
    icono: 'pi-calendar-times',
    tono: 'aviso',
    titulo: 'Ese alojamiento ya no está disponible',
    descripcion: 'Alguien reservó esas fechas antes que tú. No se hizo ningún cobro.',
  },
  pasarela: {
    icono: 'pi-exclamation-triangle',
    tono: 'aviso',
    titulo: 'El servicio de pagos no responde',
    descripcion: 'No se hizo ningún cobro. Espera un momento e inténtalo de nuevo.',
  },
  conflicto: {
    icono: 'pi-exclamation-triangle',
    tono: 'aviso',
    titulo: 'Tu reserva necesita revisión',
    descripcion: 'Revisa tu historial de reservas o contacta a soporte antes de volver a pagar.',
  },
};

@Component({
  selector: 'app-resultado-reserva',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './resultado-reserva.html',
  styleUrl: './resultado-reserva.scss',
})
export class ResultadoReserva implements OnInit {
  private ruta = inject(ActivatedRoute);
  private reservaService = inject(ReservaService);
  private alojamientoService = inject(AlojamientoService);

  tipo = signal<TipoResultado>('exito');
  config = signal<ConfigResultado>(RESULTADOS['exito']);
  mensaje = signal<string | null>(null);
  /** Alojamiento al que volver (por ejemplo, para elegir otras fechas). */
  idPublicacion = signal<number | null>(null);

  cargando = signal(false);
  errorCarga = signal<string | null>(null);
  reserva = signal<ReservaDTO | null>(null);
  alojamiento = signal<AlojamientoDetalleDTO | null>(null);

  ngOnInit(): void {
    const q = this.ruta.snapshot.queryParamMap;
    const tipoParam = q.get('tipo') as TipoResultado;
    const tipo = TIPOS_VALIDOS.includes(tipoParam) ? tipoParam : 'conflicto';
    this.tipo.set(tipo);
    this.config.set(RESULTADOS[tipo]);

    const mensaje = q.get('mensaje');
    if (mensaje) this.mensaje.set(mensaje.slice(0, 300));

    const id = Number(q.get('id'));
    if (tipo === 'exito') {
      if (Number.isInteger(id) && id > 0) {
        this.cargarReserva(id);
      } else {
        this.errorCarga.set('No encontramos el código de la reserva. Revisa tu historial.');
      }
    } else if (Number.isInteger(id) && id > 0) {
      this.idPublicacion.set(id);
    }
  }

  private cargarReserva(idReserva: number): void {
    this.cargando.set(true);
    this.reservaService.consultarEstado(idReserva).subscribe({
      next: (reserva) => {
        this.reserva.set(reserva);
        if (reserva.idPublicacion) {
          this.idPublicacion.set(reserva.idPublicacion);
          this.alojamientoService.obtenerDetalle(reserva.idPublicacion).subscribe({
            next: (aloj) => this.alojamiento.set(aloj),
            // El resumen se puede mostrar sin la foto ni el título
            error: () => this.alojamiento.set(null),
          });
        }
        this.cargando.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.errorCarga.set(
          err.status === 404 || err.status === 403
            ? 'No encontramos esta reserva en tu cuenta.'
            : 'No pudimos cargar los datos de la reserva. Revisa tu historial.',
        );
      },
    });
  }

  aFecha(iso: string | undefined): Date | null {
    if (!iso) return null;
    const [a, m, d] = iso.split('-').map(Number);
    return new Date(a, m - 1, d);
  }

  noches(reserva: ReservaDTO): number {
    const inicio = this.aFecha(reserva.fechaLlegada);
    const fin = this.aFecha(reserva.fechaSalida);
    if (!inicio || !fin) return 0;
    return Math.round((fin.getTime() - inicio.getTime()) / 86400000);
  }
}
