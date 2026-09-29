import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { mensajeDeError } from '../../services/http-error';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html', 
  styleUrl: './login.css'      
})
export class LoginComponent {
  username = '';
  password = '';
  errorMessage = signal('');
  enviando = signal(false);

  private authService = inject(AuthService);
  private router = inject(Router);

  iniciarSesion() {
    this.errorMessage.set('');
    this.enviando.set(true);
    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.enviando.set(false);
        this.router.navigate(['/envios']);
      },
      error: (err) => {
        this.enviando.set(false);
        this.errorMessage.set(mensajeDeError(err, 'Usuario o contraseña incorrectos.'));
      }
    });
  }
}