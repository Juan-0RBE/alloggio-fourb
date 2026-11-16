import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  imports: [CommonModule, FormsModule, RouterLink],
  selector: 'app-recuperacion.component',
  styleUrl: './recuperacion.scss',
  templateUrl: './recuperacion.html',
})
export class Recuperacion {
  alEnviarRecuperacion() {
    console.log('Solicitud de recuperación enviada');
  }
}
