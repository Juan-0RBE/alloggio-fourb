import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL } from './auth.service';
import { CancelacionDTO, CotizacionDTO, ReservaDTO } from '../model/reserva.model';

@Injectable({ providedIn: 'root' })
export class ReservaService {
  private http = inject(HttpClient);
  private readonly apiCotizacion = `${API_URL}/cotizacion`;
  private readonly apiReserva = `${API_URL}/reserva`;

  // Cotización

  cotizar(idPublicacion: number, fechaLlegada: string, fechaSalida: string): Observable<CotizacionDTO> {
    return this.http.post<CotizacionDTO>(`${this.apiCotizacion}/crear`, {
      idPublicacion,
      fechaLlegada,
      fechaSalida,
    });
  }

  agregarServicios(idCotizacion: number, idsServicios: number[]): Observable<CotizacionDTO> {
    return this.http.post<CotizacionDTO>(
      `${this.apiCotizacion}/servicios/${idCotizacion}`,
      idsServicios,
    );
  }

  // Reserva

  /**
   * Crea la reserva y cobra la cotización. `metodoPago` es el id del PaymentMethod
   * de Stripe (pm_...); los datos de la tarjeta nunca pasan por nuestro back.
   */
  crearReserva(
    idCotizacion: number,
    metodoPago: string,
    claveIdempotencia: string,
  ): Observable<ReservaDTO> {
    return this.http.post<ReservaDTO>(`${this.apiReserva}/crear`, {
      idCotizacion,
      metodoPago,
      claveIdempotencia,
    });
  }

  /** Reservas del huésped autenticado, de la llegada más reciente a la más antigua. */
  obtenerHistorial(): Observable<ReservaDTO[]> {
    return this.http
      .get<ReservaDTO[] | null>(`${this.apiReserva}/historial`)
      .pipe(map((lista) => lista ?? []));
  }

  consultarEstado(idReserva: number): Observable<ReservaDTO> {
    return this.http.get<ReservaDTO>(`${this.apiReserva}/estado/${idReserva}`);
  }

  /** Cancela una reserva CONFIRMADA; el back aplica la política y devuelve el dinero por Stripe. */
  cancelar(idReserva: number, motivo: string): Observable<CancelacionDTO> {
    return this.http.post<CancelacionDTO>(`${this.apiReserva}/cancelar/${idReserva}`, { motivo });
  }
}
