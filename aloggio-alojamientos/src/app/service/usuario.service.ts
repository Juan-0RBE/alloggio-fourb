import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ActualizarPerfilDTO, PerfilDTO } from '../model/usuario.model';


@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly api = 'http://localhost:8081/usuario'; // o tu environment.apiUrl

  constructor(private http: HttpClient) {}

  obtenerPerfil(): Observable<PerfilDTO> {
    return this.http.get<PerfilDTO>(`${this.api}/perfil`);
  }

  actualizarPerfil(datos: ActualizarPerfilDTO): Observable<string> {
    // El back responde texto plano en el PUT
    return this.http.put(`${this.api}/perfil`, datos, { responseType: 'text' });
  }

  cambiarClave(claveActual: string, claveNueva: string): Observable<string> {
    return this.http.put(
      `${this.api}/perfil/clave`,
      { claveActual, claveUsuario: claveNueva },
      { responseType: 'text' },
    );
  }

  desactivarCuenta(): Observable<string> {
    return this.http.post(`${this.api}/desactivar`, { confirmar: true }, { responseType: 'text' });
  }

  


}
