import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.AUTH_URL}login`;

  /** Nombre del usuario autenticado; null cuando no hay sesión. */
  readonly usuario = signal<string | null>(this.getToken() ? sessionStorage.getItem('username') : null);

  login(username: string, password: string): Observable<any> {
    return this.http.post(this.apiUrl, { username, password }).pipe(
      tap((response: any) => {
        if (response.token) {
          sessionStorage.setItem('jwt_token', response.token);
          sessionStorage.setItem('username', response.username);
          sessionStorage.setItem('roles', JSON.stringify(response.roles));
          this.usuario.set(response.username);
        }
      })
    );
  }

  getToken(): string | null {
    return sessionStorage.getItem('jwt_token');
  }

  estaAutenticado(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    sessionStorage.clear();
    this.usuario.set(null);
  }
}
