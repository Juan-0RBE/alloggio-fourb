import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from './auth.service';

export type FormatoExportacion = 'PDF' | 'EXCEL';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);

  getPerfil(): Observable<any> {
    return this.http.get<any>(`${API_URL}/usuario/perfil`);
  }

  getIndicadores(): Observable<any> {
    return this.http.get<any>(`${API_URL}/dashboard/indicadores`);
  }

  exportarIndicadores(formato: FormatoExportacion): Observable<Blob> {
    const params = new HttpParams().set('formato', formato);
    return this.http.get(`${API_URL}/dashboard/exportar`, { params, responseType: 'blob' });
  }

  listarPublicaciones(): Observable<any[] | null> {
    return this.http.get<any[] | null>(`${API_URL}/moderacion/publicacion/listar`);
  }

  bloquearPublicacion(idPublicacion: number, motivoBloqueo: string): Observable<string> {
    return this.http.post(
      `${API_URL}/moderacion/publicacion/bloquear/${idPublicacion}`,
      { motivoBloqueo },
      { responseType: 'text' },
    );
  }

  listarUsuarios(): Observable<any[] | null> {
    return this.http.get<any[] | null>(`${API_URL}/moderacion/usuario/listar`);
  }

  restringirUsuario(idUsuario: number): Observable<string> {
    return this.http.post(
      `${API_URL}/moderacion/usuario/restringir/${idUsuario}`,
      { confirmar: true },
      { responseType: 'text' },
    );
  }

  listarResenas(idPublicacion: number): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/resena/alojamiento/${idPublicacion}`);
  }

  ocultarResena(idResena: number): Observable<string> {
    return this.http.post(
      `${API_URL}/resena/ocultar/${idResena}`,
      { confirmar: true },
      { responseType: 'text' },
    );
  }

  consultarAuditoria(
    filtros: Record<string, string | number | null | undefined>,
  ): Observable<any[]> {
    let params = new HttpParams();
    for (const [clave, valor] of Object.entries(filtros)) {
      if (valor !== null && valor !== undefined && valor !== '') {
        params = params.set(clave, valor);
      }
    }
    return this.http.get<any[]>(`${API_URL}/auditoria/consultar`, { params });
  }
}
