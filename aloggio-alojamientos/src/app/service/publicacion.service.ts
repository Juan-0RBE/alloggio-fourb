import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL } from './auth.service';
import {
  ImagenAlojamientoDTO,
  PoliticaCancelacionDTO,
  PublicacionDTO,
  ReglaAlojamientoDTO,
  ServicioAlojamientoDTO,
} from '../model/publicacion.model';

@Injectable({ providedIn: 'root' })
export class PublicacionService {
  private http = inject(HttpClient);
  private readonly api = `${API_URL}/publicacion`;
  private readonly apiPoliticas = `${API_URL}/politica-cancelacion`;

  // Publicaciones

  obtenerMisPublicaciones(): Observable<PublicacionDTO[]> {
    return this.http
      .get<PublicacionDTO[] | null>(`${this.api}/mis-publicaciones`)
      .pipe(map((lista) => lista ?? []));
  }

  crear(dto: PublicacionDTO): Observable<string> {
    return this.http.post(`${this.api}/crear`, dto, { responseType: 'text' });
  }

  actualizar(id: number, dto: PublicacionDTO): Observable<string> {
    return this.http.put(`${this.api}/actualizar/${id}`, dto, { responseType: 'text' });
  }

  eliminar(id: number): Observable<string> {
    return this.http.delete(`${this.api}/eliminar/${id}`, { responseType: 'text' });
  }

  activar(id: number): Observable<string> {
    return this.http.put(`${this.api}/activar/${id}`, null, { responseType: 'text' });
  }

  pausar(id: number): Observable<string> {
    return this.http.put(`${this.api}/pausar/${id}`, null, { responseType: 'text' });
  }

  // Imágenes (el back responde 204 sin cuerpo cuando no hay, por eso el ?? [])

  obtenerImagenes(idPublicacion: number): Observable<ImagenAlojamientoDTO[]> {
    return this.http
      .get<ImagenAlojamientoDTO[] | null>(`${this.api}/${idPublicacion}/imagenes`)
      .pipe(map((lista) => lista ?? []));
  }

  agregarImagen(idPublicacion: number, dto: ImagenAlojamientoDTO): Observable<string> {
    return this.http.post(`${this.api}/${idPublicacion}/imagenes/crear`, dto, {
      responseType: 'text',
    });
  }

  eliminarImagen(idImagen: number): Observable<string> {
    return this.http.delete(`${this.api}/imagenes/eliminar/${idImagen}`, { responseType: 'text' });
  }

  // Servicios

  obtenerServicios(idPublicacion: number): Observable<ServicioAlojamientoDTO[]> {
    return this.http
      .get<ServicioAlojamientoDTO[] | null>(`${this.api}/${idPublicacion}/servicios`)
      .pipe(map((lista) => lista ?? []));
  }

  agregarServicio(idPublicacion: number, dto: ServicioAlojamientoDTO): Observable<string> {
    return this.http.post(`${this.api}/${idPublicacion}/servicios/crear`, dto, {
      responseType: 'text',
    });
  }

  eliminarServicio(idServicio: number): Observable<string> {
    return this.http.delete(`${this.api}/servicios/eliminar/${idServicio}`, {
      responseType: 'text',
    });
  }

  // Reglas

  obtenerReglas(idPublicacion: number): Observable<ReglaAlojamientoDTO[]> {
    return this.http
      .get<ReglaAlojamientoDTO[] | null>(`${this.api}/${idPublicacion}/reglas`)
      .pipe(map((lista) => lista ?? []));
  }

  agregarRegla(idPublicacion: number, dto: ReglaAlojamientoDTO): Observable<string> {
    return this.http.post(`${this.api}/${idPublicacion}/reglas/crear`, dto, {
      responseType: 'text',
    });
  }

  eliminarRegla(idRegla: number): Observable<string> {
    return this.http.delete(`${this.api}/reglas/eliminar/${idRegla}`, { responseType: 'text' });
  }

  // Políticas de cancelación

  obtenerPoliticas(): Observable<PoliticaCancelacionDTO[]> {
    return this.http
      .get<PoliticaCancelacionDTO[] | null>(`${this.apiPoliticas}/obtener/todas`)
      .pipe(map((lista) => lista ?? []));
  }
}
