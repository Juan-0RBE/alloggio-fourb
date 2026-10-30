import { Routes } from '@angular/router';
import { Login } from './autenticacion/login/login.component';
import { Registro } from './autenticacion/registro/registro.component';
import { Recuperacion } from './autenticacion/recuperacion/recuperacion.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'recuperacion', component: Recuperacion },
];
