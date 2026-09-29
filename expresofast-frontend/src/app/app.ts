import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './services/auth';

@Component({
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly enlaces = [
    { ruta: '/envios', texto: 'Envíos' },
    { ruta: '/nuevo-envio', texto: 'Nuevo envío' },
    { ruta: '/rastreo', texto: 'Rastrear guía' },
  ];

  cerrarSesion(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
