import { HttpErrorResponse } from '@angular/common/http';

/** Traduce un error HTTP del backend a un mensaje legible para el usuario. */
export function mensajeDeError(err: unknown, porDefecto = 'Ocurrió un error inesperado.'): string {
  if (!(err instanceof HttpErrorResponse)) {
    return porDefecto;
  }
  if (err.status === 0) {
    return 'No se pudo conectar con el servidor. Verifique que el backend esté encendido en el puerto 8080 y que CORS permita http://localhost:4200.';
  }
  const cuerpo = err.error;
  if (cuerpo?.errores && typeof cuerpo.errores === 'object') {
    return Object.values(cuerpo.errores).join(' ');
  }
  return cuerpo?.error || cuerpo?.message || `${porDefecto} (HTTP ${err.status})`;
}
