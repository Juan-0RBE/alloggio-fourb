import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { RestablecimientoService } from '../../../service/reestablecimiento.service';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-recuperacion.component',
  styleUrl: './recuperacion.scss',
  templateUrl: './recuperacion.html',
})
export class Recuperacion {
  correoUsuario: string = '';
  codigoVerificacion: string = '';
  nuevaClave: string = '';
  confirmarClave: string = '';

  mostrarClaveNueva = false;
  mostrarClaveConfirmar = false;
  mostrarModalVerificacion = signal(false);
  mostrarModalNuevaClave = signal(false);
  cargando = signal(false);

  // Un mensaje por sección
  errorCorreo = signal('');
  errorCodigo = signal('');
  infoCodigo = signal('');
  errorClave = signal('');

  constructor(
    private router: Router,
    private restablecimientoServ: RestablecimientoService
  ) {}

  // Paso 1: el back genera y envía el código al correo
  alEnviarRecuperacion() {
    if (!this.correoUsuario) {
      this.errorCorreo.set('El correo es obligatorio');
    }
    if (this.correoUsuario.length > 100 || !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(this.correoUsuario)) {
      this.errorCorreo.set('Correo electrónico inválido');
    }

    this.restablecimientoServ.solicitarCodigo(this.correoUsuario.trim()).subscribe({
      next: () => {
        this.cargando.set(false);
        this.codigoVerificacion = '';
        this.errorCodigo.set('');
        this.infoCodigo.set('');
        this.mostrarModalVerificacion.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.cargando.set(false);
        this.errorCorreo.set(this.obtenerMensaje(err));
      },
    });
  }

  soloNumeros(event: Event) {
    const input = event.target as HTMLInputElement;
    const limpio = input.value.replace(/\D/g, '').slice(0, 6);
    input.value = limpio;
    this.codigoVerificacion = limpio;
  }

  verificarCodigo() {
    this.errorCodigo.set('');
    this.infoCodigo.set('');

    if (!/^\d{6}$/.test(this.codigoVerificacion)) {
      this.errorCodigo.set('El código debe tener 6 dígitos numéricos.');
      return;
    }
    if (this.cargando()) return;
    this.cargando.set(true);

    this.restablecimientoServ
      .verificarCodigo(this.correoUsuario.trim(), this.codigoVerificacion)
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.errorClave.set('');
          this.mostrarModalVerificacion.set(false);
          this.mostrarModalNuevaClave.set(true);
        },
        error: (err: HttpErrorResponse) => {
          this.cargando.set(false);
          this.errorCodigo.set(this.obtenerMensaje(err));
        },
      });
  }

  // Paso 3: se envían correo + código + nueva clave
  actualizarContrasena() {
    const c = this.nuevaClave;
    if (c.length < 6 || c.length > 20) this.errorClave.set('La contraseña debe tener entre 6 y 20 caracteres');
    if (!/[A-Z]/.test(c)) this.errorClave.set('La contraseña debe tener una mayúscula');
    if (!/[a-z]/.test(c)) this.errorClave.set('La contraseña debe tener una minúscula');
    if (!/[0-9]/.test(c)) this.errorClave.set('La contraseña debe tener un número');
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(c)) this.errorClave.set('La contraseña debe tener un carácter especial (!@#$%^&*)');
    if (c !== this.confirmarClave) this.errorClave.set('Las contraseñas no coinciden');

    if (this.cargando()) return;
    this.cargando.set(true);

    this.restablecimientoServ
      .confirmarCambio({
        correoUsuario: this.correoUsuario.trim(),
        codigo: this.codigoVerificacion.trim(),
        nuevaClave: this.nuevaClave,
      })
      .subscribe({
        next: () => {
          this.cargando.set(false);
          this.mostrarModalNuevaClave.set(false);
          this.router.navigate(['/inicio-sesion']);
        },
        error: (err: HttpErrorResponse) => {
          this.cargando.set(false);
          const mensaje = this.obtenerMensaje(err);

          // Caso raro: el código expiró mientras escribía la clave
          if (err.status === 400 && /código/i.test(mensaje)) {
            this.codigoVerificacion = '';
            this.errorCodigo.set(mensaje);
            this.mostrarModalNuevaClave.set(false);
            this.mostrarModalVerificacion.set(true);
          } else {
            this.errorClave.set(mensaje);
          }
        },
      });
  }

  reenviarCodigo() {
    this.errorCodigo.set('');
    this.infoCodigo.set('');
    this.codigoVerificacion = '';

    this.restablecimientoServ.solicitarCodigo(this.correoUsuario.trim()).subscribe({
      next: () => this.infoCodigo.set('Te enviamos un nuevo código.'),
      error: (err: HttpErrorResponse) => this.errorCodigo.set(this.obtenerMensaje(err)),
    });
  }

  cerrarModalVerificacion() {
    this.mostrarModalVerificacion.set(false);
    this.errorCodigo.set('');
    this.infoCodigo.set('');
  }

  cerrarModalNuevaClave() {
    this.mostrarModalNuevaClave.set(false);
    this.errorClave.set('');
  }

  private obtenerMensaje(err: HttpErrorResponse): string {
    return typeof err.error === 'string' && err.error
      ? err.error
      : 'No se pudo conectar con el servidor.';
  }
}
