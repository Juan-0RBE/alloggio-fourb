import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './auth.service';
import { ResenaDTO } from '../model/alojamiento.model';

@Injectable({ providedIn: 'root' })
export class ResenaService {
  private http = inject(HttpClient);
  private readonly api = `${API_URL}/resena`;

  /** Crea la reseña de una reserva COMPLETADA propia (una sola por reserva). */
  crear(idReserva: number, calificacion: number, comentario: string): Observable<ResenaDTO> {
    return this.http.post<ResenaDTO>(`${this.api}/crear`, { idReserva, calificacion, comentario });
  }
}
