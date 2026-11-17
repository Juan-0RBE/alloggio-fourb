import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_URL } from './auth.service';
import {
  AlojamientoDetalleDTO,
  AlojamientoResumenDTO,
  CriteriosBusqueda,
  PaginaDTO,
} from '../model/alojamiento.model';

/** Consultas públicas de alojamientos (no requieren sesión). */
@Injectable({ providedIn: 'root' })
export class AlojamientoService {
  private http = inject(HttpClient);
  private readonly api = `${API_URL}/alojamiento`;

  buscar(criterios: CriteriosBusqueda): Observable<PaginaDTO<AlojamientoResumenDTO>> {
    let params = new HttpParams()
      .set('pagina', criterios.pagina ?? 0)
      .set('tamano', criterios.tamano ?? 10);

    const ciudad = criterios.ciudad?.trim();
    if (ciudad) params = params.set('ciudad', ciudad);
    if (criterios.fechaLlegada && criterios.fechaSalida) {
      params = params
        .set('fechaLlegada', criterios.fechaLlegada)
        .set('fechaSalida', criterios.fechaSalida);
    }
    if (criterios.precioMin != null) params = params.set('precioMin', criterios.precioMin);
    if (criterios.precioMax != null) params = params.set('precioMax', criterios.precioMax);
    if (criterios.capacidad) params = params.set('capacidad', criterios.capacidad);
    if (criterios.tipo) params = params.set('tipo', criterios.tipo);
    for (const servicio of criterios.servicios ?? []) {
      params = params.append('servicios', servicio);
    }

    return this.http.get<PaginaDTO<AlojamientoResumenDTO>>(`${this.api}/buscar`, { params });
  }

  /** Detalle público de un alojamiento ACTIVO. El back responde 404 si no existe o no está activo. */
  obtenerDetalle(idPublicacion: number): Observable<AlojamientoDetalleDTO> {
    return this.http.get<AlojamientoDetalleDTO>(`${this.api}/detalle/${idPublicacion}`).pipe(
      map((d) => ({
        ...d,
        tarifaLimpieza: d.tarifaLimpieza ?? 0,
        servicios: d.servicios ?? [],
        reglas: d.reglas ?? [],
        imagenes: [...(d.imagenes ?? [])].sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0)),
        calendarioOcupado: d.calendarioOcupado ?? [],
        totalResenas: d.totalResenas ?? 0,
        resenas: d.resenas ?? [],
      })),
    );
  }
}
