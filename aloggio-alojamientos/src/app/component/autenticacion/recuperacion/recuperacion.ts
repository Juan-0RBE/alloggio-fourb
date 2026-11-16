import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-recuperacion.component',
  styleUrl: './recuperacion.scss',
  templateUrl: './recuperacion.html',
})
export class Recuperacion {
  correo: string = '';
  codigoVerificacion: string = '';
  nuevaClave: string = '';
  confirmarClave: string = '';

  mostrarModalVerificacion: boolean = false;
  mostrarModalNuevaClave: boolean = false;

  mostrarClaveNueva: boolean = false;
  mostrarClaveConfirmar: boolean = false;

  constructor(private router: Router) {}

  alEnviarRecuperacion() {
    if (!this.correo) return;
    this.mostrarModalVerificacion = true;
  }

  verificarCodigo() {
    this.mostrarModalVerificacion = false;
    this.mostrarModalNuevaClave = true;
  }

  actualizarContrasena() {
    if (this.nuevaClave !== this.confirmarClave) {
      alert('Las contraseñas no coinciden.');
      return;
    }
    alert('¡Contraseña restablecida correctamente!');
    this.mostrarModalNuevaClave = false;
    this.router.navigate(['/inicio-sesion']);
  }

  reenviarCodigo() {
    alert('Nuevo código de recuperación enviado.');
  }
}
