import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

const MENSAJES_ERROR: Record<number, string> = {
  401: 'Nombre de usuario o contraseña inválidos.',
  403: 'Tu cuenta fue restringida por un administrador y no puede reactivarse por este medio.',
  409: 'Tu cuenta ya está activa. Puedes iniciar sesión.',
};

const MENSAJE_ERROR_GENERAL = 'No pudimos reactivar tu cuenta. Inténtalo de nuevo.';

@Component({
  selector: 'app-reactivar-cuenta',
  imports: [FormsModule, RouterLink, CommonModule],
  styleUrl: './reactivar-cuenta.scss',
  templateUrl: './reactivar-cuenta.html',
})
export class ReactivarCuenta {
  nombreUsuario: string = '';
  clave: string = '';
  confirmar: boolean = false;

  mostrarClave: boolean = false;
  enviando: boolean = false;
  reactivada: boolean = false;
  mensajeError: string = '';

  formularioValido(): boolean {
    return this.nombreUsuario.trim().length > 0 && this.clave.length > 0 && this.confirmar;
  }

  alReactivar() {
    if (!this.formularioValido() || this.enviando) return;

    this.mensajeError = '';
    this.enviando = true;
    setTimeout(() => {
      this.enviando = false;
      this.reactivada = true;
    }, 400);
  }

  mostrarErrorServidor(codigoHttp: number) {
    this.enviando = false;
    this.mensajeError = MENSAJES_ERROR[codigoHttp] ?? MENSAJE_ERROR_GENERAL;
  }
}
