import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  styleUrl: './login.component.scss',
  templateUrl: './login.component.html',
})
export class Login {
  mostrarClave: boolean = false;

  alIniciarSesion() {
    console.log('Iniciando sesión...');
  }
}
