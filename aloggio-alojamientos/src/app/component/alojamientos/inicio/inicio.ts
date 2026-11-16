import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

interface Alojamiento {
  id: number;
  titulo: string;
  descripcion: string;
  ciudad: string;
  tipo: string;
  precioNoche: number;
  tarifaLimpieza: number;
  capacidad: number;
  imagenUrl: string;
  etiqueta?: string;
  servicioDestacado: string;
}

@Component({
  imports: [CommonModule, FormsModule, RouterModule],
  selector: 'app-inicio.component',
  styleUrl: './inicio.scss',
  templateUrl: './inicio.html',
})
export class Inicio {
  alojamientos: Alojamiento[] = [];
}
