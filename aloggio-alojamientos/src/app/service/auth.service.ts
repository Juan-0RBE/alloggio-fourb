import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, UsuarioDTO } from '../model/usuario.model';

export const API_URL = 'http://localhost:8081';

@Injectable({ providedIn: 'root' })

export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  private readonly TOKEN_KEY = 'token';

  login(user: UsuarioDTO): Observable<AuthResponse> {
    console.log(user);
    return this.http.post<AuthResponse>(`${API_URL}/auth/login`, user).pipe(
      tap(res => sessionStorage.setItem(this.TOKEN_KEY, res.token))
    );
  }

  register(user: UsuarioDTO) {
    return this.http.post(`${API_URL}/auth/register`, user, { responseType: 'text' });
  }

  logout(): void {
    sessionStorage.removeItem(this.TOKEN_KEY);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return sessionStorage.getItem(this.TOKEN_KEY);
  }

  private payload(): { role?: string; exp: number } | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(base64));
    } catch {
      return null;
    }
  }

  isLoggedIn(): boolean {
    const p = this.payload();
    return !!p && p.exp * 1000 > Date.now();
  }

  isAdmin(): boolean {
    return this.payload()?.role === 'ROLE_ADMINISTRADOR';
  }

  // A dónde mandar a cada rol
  homeUrl(): string {
    return this.isAdmin() ? '/admin' : '/inicio';
  }
}
