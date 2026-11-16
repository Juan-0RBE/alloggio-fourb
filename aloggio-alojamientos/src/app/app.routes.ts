import { Routes } from '@angular/router';
import { Login } from './autenticacion/login/login.component';
import { Registro } from './autenticacion/registro/registro.component';
import { Recuperacion } from './autenticacion/recuperacion/recuperacion.component';
import { Inicio } from './alojamientos/inicio/inicio.component';
import { DetalleAlojamiento } from './alojamientos/detalle-alojamiento/detalle-alojamiento.component';
import { ResumenReserva } from './reservas/resumen-reserva/resumen-reserva.component';
import { ResultadoReserva } from './reservas/resultado-reserva/resultado-reserva.component';
import { MisReservas } from './reservas/mis-reservas/mis-reservas.component';
import { FormularioResena } from './resenas/formulario-resena/formulario-resena.component';
import { Usuario } from './usuarios/usuario/usuario.component';
import { Anfitrion } from './usuarios/anfitrion/anfitrion.component';
import { Admin } from './usuarios/admin/admin.component';
import { BandejaNotificaciones } from './notificaciones/bandeja-notificaciones/bandeja-notificaciones.component';
import { FormularioCancelacion } from './reservas/formulario-cancelacion/formulario-cancelacion.component';
import { DetalleReserva } from './reservas/detalle-reserva.component/detalle-reserva.component';
import { CalendarioDisponibilidad } from './usuarios/calendario-disponibilidad/calendario-disponibilidad.component';
export const routes: Routes = [
  { path: '', redirectTo: 'inicio', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'recuperacion', component: Recuperacion },

  { path: 'inicio', component: Inicio },
  { path: 'detalle-reserva/:id', component: DetalleReserva },
  { path: 'detalle-alojamiento/:id', component: DetalleAlojamiento },
  { path: 'resumen-reserva', component: ResumenReserva },
  { path: 'resultado-reserva', component: ResultadoReserva },
  { path: 'mis-reservas', component: MisReservas },
  { path: 'formulario-resena', component: FormularioResena },
  { path: 'formulario-cancelacion', component: FormularioCancelacion },

  { path: 'usuario', component: Usuario },
  { path: 'anfitrion', component: Anfitrion },
  { path: 'admin', component: Admin },
  { path: 'calendario-disponibilidad', component: CalendarioDisponibilidad },
  { path: 'notificaciones', component: BandejaNotificaciones },
];
