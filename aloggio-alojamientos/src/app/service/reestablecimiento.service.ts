import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CodigoRestablecimientoDTO {
  correoUsuario: string;
  codigo?: string;
  nuevaClave?: string;
}

@Injectable({ providedIn: 'root' })
export class RestablecimientoService {
  private http = inject(HttpClient);
  private readonly api = 'http://localhost:8081/restablecimiento'; // o tu environment.apiUrl

  solicitarCodigo(correoUsuario: string): Observable<string> {
    return this.http.post(
      `${this.api}/solicitar`,
      { correoUsuario },
      { responseType: 'text' }
    );
  }

  confirmarCambio(data: CodigoRestablecimientoDTO): Observable<string> {
    return this.http.post(`${this.api}/confirmar`, data, { responseType: 'text' });
  }

  verificarCodigo(correoUsuario: string, codigo: string): Observable<string> {
    return this.http.post(
      `${this.api}/verificar`,
      { correoUsuario, codigo },
      { responseType: 'text' }
    );
  }
}
