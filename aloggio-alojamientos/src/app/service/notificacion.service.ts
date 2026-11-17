import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';
import { API_URL } from './auth.service';
import { NotificacionDTO } from '../model/notificacion.model';

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private http = inject(HttpClient);
  private readonly api = `${API_URL}/notificacion`;

  /** Cantidad de notificaciones sin leer, para mostrar un indicador en el encabezado. */
  readonly noLeidas = signal(0);

  /** Bandeja del usuario autenticado, de la más reciente a la más antigua. */
  obtenerMisNotificaciones(): Observable<NotificacionDTO[]> {
    return this.http.get<NotificacionDTO[] | null>(`${this.api}/mis-notificaciones`).pipe(
      map((lista) => lista ?? []),
      tap((lista) => this.noLeidas.set(lista.filter((n) => !n.estaLeida).length)),
    );
  }

  marcarComoLeida(idNotificacion: number): Observable<string> {
    return this.http
      .put(`${this.api}/leer/${idNotificacion}`, null, { responseType: 'text' })
      .pipe(tap(() => this.noLeidas.update((n) => Math.max(0, n - 1))));
  }
}
