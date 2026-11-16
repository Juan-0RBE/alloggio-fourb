import { Routes } from '@angular/router';
import { Login } from './component/autenticacion/login/login';
import { Registro } from './component/autenticacion/registro/registro';
import { Recuperacion } from './component/autenticacion/recuperacion/recuperacion';
import { Inicio } from './component/alojamientos/inicio/inicio';
import { DetalleAlojamiento } from './component/alojamientos/detalle-alojamiento/detalle-alojamiento';
import { ResumenReserva } from './component/reservas/resumen-reserva/resumen-reserva';
import { ResultadoReserva } from './component/reservas/resultado-reserva/resultado-reserva';
import { MisReservas } from './component/reservas/mis-reservas/mis-reservas';
import { FormularioResena } from './component/resenas/formulario-resena/formulario-resena';
import { Usuario } from './component/usuarios/usuario/usuario';
import { ReactivarCuenta } from './component/autenticacion/reactivar-cuenta/reactivar-cuenta';
import { Anfitrion } from './component/usuarios/anfitrion/anfitrion';
import { Admin } from './component/usuarios/admin/admin';
//import { adminGuard, userGuard } from './service/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'recuperacion', component: Recuperacion },
  { path: 'inicio', component: Inicio },
  { path: 'reactivar-cuenta', component: ReactivarCuenta },

  {
    path: '',
    //    canActivate: [userGuard],
    children: [
      { path: 'detalle-alojamiento/:id', component: DetalleAlojamiento },
      { path: 'resumen-reserva', component: ResumenReserva },
      { path: 'resultado-reserva', component: ResultadoReserva },
      { path: 'mis-reservas', component: MisReservas },
      { path: 'formulario-resena', component: FormularioResena },
      { path: 'usuario', component: Usuario },
      { path: 'anfitrion', component: Anfitrion },
    ],
  },

  { path: 'admin', component: Admin /*canActivate: [adminGuard] */ },
  { path: '**', redirectTo: 'login' },
];
