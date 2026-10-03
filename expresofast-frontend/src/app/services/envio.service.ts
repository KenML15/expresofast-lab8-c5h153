import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CrearEnvioPayload,
  Envio,
  EnvioConPaquetes,
  EnvioRegistroPayload,
  EstadoEnvio,
  TrackingCheck,
} from '../models/envio.model';

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

  // ===================== Laboratorio 11 =====================

  /** Consulta si un número de rastreo ya existe. Lo consume el validador asíncrono. */
  verificarTracking(numeroTracking: string): Observable<boolean> {
    return this.http
      .get<TrackingCheck>(`${this.apiUrl}/check-tracking/${encodeURIComponent(numeroTracking.trim())}`)
      .pipe(map((respuesta) => respuesta.existe));
  }

  /** Registra el envío con todos sus paquetes en una sola petición (transacción en el backend). */
  registrarEnvioAvanzado(payload: EnvioRegistroPayload): Observable<EnvioConPaquetes> {
    return this.http.post<EnvioConPaquetes>(`${this.apiUrl}/avanzado`, payload);
  }
}