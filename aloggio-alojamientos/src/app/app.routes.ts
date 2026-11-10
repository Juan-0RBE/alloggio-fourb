import { Routes } from '@angular/router';
import { Login } from './autenticacion/login/login.component';
import { Registro } from './autenticacion/registro/registro.component';
import { Recuperacion } from './autenticacion/recuperacion/recuperacion.component';
import { Inicio } from './alojamientos/inicio/inicio.component';
import { DetalleAlojamiento } from './alojamientos/detalle-alojamiento/detalle-alojamiento.component';
import { ResumenReserva } from './alojamientos/resumen-reserva/resumen-reserva.component';
import { FormularioResena } from './resenas/formulario-resena/formulario-resena.component';

export const routes: Routes = [
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'recuperacion', component: Recuperacion },

  { path: 'inicio', component: Inicio },
  { path: 'detalle-alojamiento/:id', component: DetalleAlojamiento },
  { path: 'resumen-reserva', component: ResumenReserva },
  { path: 'formulario-resena', component: FormularioResena },
];
