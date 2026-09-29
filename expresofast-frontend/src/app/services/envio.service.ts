import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CrearEnvioPayload, Envio, EstadoEnvio } from '../models/envio.model';

@Injectable({
  providedIn: 'root',
})
export class EnvioService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.API_URL}envios`;

  obtenerEnvios(): Observable<Envio[]> {
    return this.http.get<Envio[]>(this.apiUrl);
  }

  obtenerPorRastreo(codigo: string): Observable<Envio> {
    return this.http.get<Envio>(`${this.apiUrl}/rastreo/${encodeURIComponent(codigo.trim())}`);
  }

  crearEnvio(payload: CrearEnvioPayload): Observable<Envio> {
    return this.http.post<Envio>(this.apiUrl, payload);
  }

  actualizarEstado(id: number, nuevoEstado: EstadoEnvio, observaciones?: string): Observable<Envio> {
    return this.http.patch<Envio>(`${this.apiUrl}/${id}/estado`, { nuevoEstado, observaciones });
  }
}
