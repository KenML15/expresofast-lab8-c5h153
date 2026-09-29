import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { EnvioListComponent } from './components/envio-list/envio-list.component';
import { EnvioFormComponent } from './components/envio-form/envio-form.component';
import { EnvioTrackingComponent } from './components/envio-tracking/envio-tracking.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'envios', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, title: 'Iniciar sesión · ExpresoFast' },
  { path: 'envios', component: EnvioListComponent, canActivate: [authGuard], title: 'Envíos · ExpresoFast' },
  { path: 'nuevo-envio', component: EnvioFormComponent, canActivate: [authGuard], title: 'Nuevo envío · ExpresoFast' },
  { path: 'rastreo', component: EnvioTrackingComponent, canActivate: [authGuard], title: 'Rastrear guía · ExpresoFast' },
  { path: '**', redirectTo: 'envios' },
];
