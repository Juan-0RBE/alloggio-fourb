import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL } from './auth.service';
import { PeriodoDisponibilidadDTO } from '../model/disponibilidad.model';

@Injectable({ providedIn: 'root' })
export class DisponibilidadService {
  private http = inject(HttpClient);
  private readonly api = `${API_URL}/disponibilidad`;

  /** Periodos bloqueados y reservados de una publicación propia. */
  consultarCalendario(idPublicacion: number): Observable<PeriodoDisponibilidadDTO[]> {
    return this.http
      .get<PeriodoDisponibilidadDTO[] | null>(`${this.api}/calendario/${idPublicacion}`)
      .pipe(map((lista) => lista ?? []));
  }

  /** Bloquea [fechaInicio, fechaFin). La fecha de fin es el día de salida y no se incluye. */
  bloquearRango(idPublicacion: number, fechaInicio: string, fechaFin: string): Observable<string> {
    const dto: PeriodoDisponibilidadDTO = { idPublicacion, fechaInicio, fechaFin };
    return this.http.post(`${this.api}/bloquear`, dto, { responseType: 'text' });
  }

  desbloquearRango(idPeriodo: number): Observable<string> {
    return this.http.delete(`${this.api}/desbloquear/${idPeriodo}`, { responseType: 'text' });
  }

  estaDisponible(idPublicacion: number, fechaInicio: string, fechaFin: string): Observable<boolean> {
    const params = new HttpParams()
      .set('idPublicacion', idPublicacion)
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    return this.http.get<boolean>(`${this.api}/verificar`, { params });
  }
}
