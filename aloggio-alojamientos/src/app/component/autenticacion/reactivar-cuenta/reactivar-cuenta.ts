import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../../../service/auth.service';
import { finalize } from 'rxjs';

const MENSAJES_ERROR: Record<number, string> = {
  0: 'No pudimos conectar con el servidor. Revisa tu conexión.',
  401: 'Nombre de usuario o contraseña inválidos.',
  403: 'Tu cuenta fue restringida por un administrador y no puede reactivarse por este medio.',
  409: 'Tu cuenta ya está activa. Puedes iniciar sesión.',
  422: 'Operación cancelada: tu cuenta continúa inactiva.',
};

const MENSAJE_ERROR_GENERAL = 'No pudimos reactivar tu cuenta. Inténtalo de nuevo.';

@Component({
  selector: 'app-reactivar-cuenta',
  imports: [FormsModule, RouterLink, CommonModule],
  styleUrl: './reactivar-cuenta.scss',
  templateUrl: './reactivar-cuenta.html',
})
export class ReactivarCuenta {
  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);

  nombreUsuario = '';
  clave = '';
  confirmar = false;

  mostrarClave = false;
  enviando = false;
  reactivada = false;
  mensajeError = '';

  formularioValido(): boolean {
    return this.nombreUsuario.trim().length > 0 && this.clave.length > 0 && this.confirmar;
  }

  alReactivar() {
    const esCorreo = this.nombreUsuario.includes('@');
    const reactivarRequest = {
      nombreUsuario: esCorreo ? null : this.nombreUsuario.trim(),
      correoUsuario: esCorreo ? this.nombreUsuario.trim() : null,
      claveUsuario: this.clave,
      confirmar: this.confirmar,
    };
    if (!this.formularioValido() || this.enviando) return;

    this.mensajeError = '';
    this.enviando = true;

    // @ts-ignore
    this.authService.reactivarCuenta(reactivarRequest)
      .pipe(
        finalize(() => {
          this.enviando = false;   // se ejecuta en éxito, error o complete
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: () => {
          this.reactivada = true;
          this.clave = '';
        },
        error: (err: HttpErrorResponse) => {
          this.mensajeError = MENSAJES_ERROR[err.status] ?? MENSAJE_ERROR_GENERAL;
        },
      });
  }

  mostrarErrorServidor(codigoHttp: number) {
    this.enviando = false;
    this.mensajeError = MENSAJES_ERROR[codigoHttp] ?? MENSAJE_ERROR_GENERAL;
  }
}
