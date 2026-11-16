import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../service/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  styleUrl: './login.scss',
  templateUrl: './login.html',
})
export class Login {
  loginData = {
    correo: '',
    password: '',
  };

  mostrarClave: boolean = false;
  isLoading = false;
  errorMessage = '';
  successMessage: string = '';
  cuentaInactiva = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  onSubmit() {
    this.errorMessage = '';
    this.successMessage = '';
    this.cuentaInactiva = false;

    if (!this.loginData.correo || !this.loginData.password) {
      this.errorMessage = 'Completa todos los campos';
      return;
    }
    if (this.isLoading) return;
    this.isLoading = true;

    const esCorreo = this.loginData.correo.includes('@');

    const loginRequest = {
      nombreUsuario: esCorreo ? null : this.loginData.correo,
      correoUsuario: esCorreo ? this.loginData.correo : null,
      claveUsuario: this.loginData.password,
    };

    console.log('Enviando al back:', loginRequest);
    // @ts-ignore
    this.authService.login(loginRequest).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = '¡Inicio de sesión exitoso!';
        this.cdr.detectChanges();

        setTimeout(() => this.router.navigate([this.authService.homeUrl()]), 800);
      },
      error: (error) => {
        this.isLoading = false;
        this.errorMessage = this.getErrorMessage(error);
        this.cuentaInactiva =
          error.status === 403 &&
          typeof error.error === 'string' &&
          error.error.includes('inactiva');
        this.cdr.detectChanges();
      },
    });
  }

  private getErrorMessage(error: any): string {
    switch (error.status) {
      case 0:
        return 'No hay conexión con el servidor.';
      case 400:
        return 'Datos inválidos. Revisa el formato.';
      case 401:
        return 'Usuario o contraseña incorrectos.';
      case 403:
        return typeof error.error === 'string' && error.error
          ? error.error
          : 'No tienes permiso para acceder.';
      case 500:
        return 'Error interno del servidor. Intenta más tarde.';
      default:
        return error.error?.message || 'Error al iniciar sesión.';
    }
  }
}
