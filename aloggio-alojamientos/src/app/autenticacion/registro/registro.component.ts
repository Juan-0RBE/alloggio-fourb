import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink, CommonModule],
  styleUrl: './registro.component.scss',
  templateUrl: './registro.component.html',
})
export class Registro {
  mostrarContrasena: boolean = false;

  pasoActual: number = 1;
  mostrarClavePrincipal: boolean = false;
  mostrarClaveConfirmar: boolean = false;

  formulario = {
    correoUsuario: '',
    nombreUsuario: '',
    claveUsuario: '',
    confirmarClave: '',
    rolUsuario: 'HUESPED',
    documentoIdentidad: '',
    primerNombre: '',
    segundoNombre: '',
    primerApellido: '',
    segundoApellido: '',
    fechaNacimiento: '',
    numeroTelefono: '',
  };

  siguientePaso() {
    if (this.formulario.claveUsuario !== this.formulario.confirmarClave) {
      alert('Las contraseñas no coinciden.');
      return;
    }
    this.pasoActual = 2;
  }

  alRegistrar() {
    console.log('Registro exitoso...');
  }
}
