import {
  AbstractControl,
  AsyncValidatorFn,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { Observable, catchError, map, of, switchMap, take, timer } from 'rxjs';
import { EnvioService } from '../services/envio.service';


export const fechasEnvioValidator: ValidatorFn = (
  grupo: AbstractControl,
): ValidationErrors | null => {
  const despacho = grupo.get('fechaDespacho')?.value as string | undefined;
  const entrega = grupo.get('fechaEntregaEstimada')?.value as string | undefined;


  if (!despacho || !entrega) {
    return null;
  }

  const tiempoDespacho = new Date(despacho).getTime();
  const tiempoEntrega = new Date(entrega).getTime();

  return tiempoEntrega > tiempoDespacho ? null : { rangoFechasInvalido: true };
};


export function trackingDisponibleValidator(envioService: EnvioService): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    const valor = ((control.value as string) ?? '').trim();


    if (!valor) {
      return of(null);
    }

    return timer(400).pipe(
      switchMap(() => envioService.verificarTracking(valor)),
      map((existe) => (existe ? { trackingTomado: true } : null)),
      // Si el API falla, NO dejamos pasar el tracking sin verificar
      catchError(() => of({ trackingNoVerificado: true })),
      take(1), // garantiza que el Observable se complete (Angular lo exige)
    );
  };
}