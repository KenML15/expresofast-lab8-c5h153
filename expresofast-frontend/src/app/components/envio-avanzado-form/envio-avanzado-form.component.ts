import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormArray,
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EnvioService } from '../../services/envio.service';
import { mensajeDeError } from '../../services/http-error';
import { EnvioConPaquetes, EnvioRegistroPayload } from '../../models/envio.model';
import { fechasEnvioValidator, trackingDisponibleValidator } from '../../validators/envio.validators';


type PaqueteForm = FormGroup<{
  descripcion: FormControl<string>;
  pesoKg: FormControl<number | null>; 
}>;


type EnvioAvanzadoForm = FormGroup<{
  numeroTracking: FormControl<string>;
  destinatario: FormControl<string>;
  direccionDestino: FormControl<string>;
  montoFlete: FormControl<number | null>;
  fechaDespacho: FormControl<string>;       
  fechaEntregaEstimada: FormControl<string>; 
  paquetes: FormArray<PaqueteForm>;
}>;

@Component({
  selector: 'app-envio-avanzado-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './envio-avanzado-form.component.html',
  styleUrl: './envio-avanzado-form.component.css',
})
export class EnvioAvanzadoFormComponent {
  private fb = inject(NonNullableFormBuilder);
  private envioService = inject(EnvioService);

  enviando = signal(false);
  error = signal('');
  creado = signal<EnvioConPaquetes | null>(null);


  form: EnvioAvanzadoForm = this.fb.group(
    {
      numeroTracking: this.fb.control('', {
        validators: [
          Validators.required,
          Validators.maxLength(30),
          Validators.pattern(/^[A-Za-z0-9-]+$/),
        ],
 
        asyncValidators: [trackingDisponibleValidator(this.envioService)],
      }),
      destinatario: this.fb.control('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(120),
      ]),
      direccionDestino: this.fb.control('', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(200),
      ]),
      montoFlete: this.fb.control<number | null>(null, [Validators.required, Validators.min(1)]),
      fechaDespacho: this.fb.control('', Validators.required),
      fechaEntregaEstimada: this.fb.control('', Validators.required),


      paquetes: this.fb.array<PaqueteForm>([this.crearPaquete()]),
    },
    {

      validators: [fechasEnvioValidator],
    },
  );


  get paquetes(): FormArray<PaqueteForm> {
    return this.form.controls.paquetes;
  }


  private crearPaquete(): PaqueteForm {
    return this.fb.group({
      descripcion: this.fb.control('', [Validators.required, Validators.maxLength(255)]),
      pesoKg: this.fb.control<number | null>(null, [
        Validators.required,
        Validators.min(0.01),
        Validators.max(999.99), 
      ]),
    });
  }


  agregarPaquete(): void {
    this.paquetes.push(this.crearPaquete());
  }


  eliminarPaquete(indice: number): void {
    if (this.paquetes.length <= 1) {
      return;
    }
    this.paquetes.removeAt(indice);
  }

  pesoTotal(): number {
    return this.paquetes.controls.reduce(
      (total, paquete) => total + (Number(paquete.controls.pesoKg.value) || 0),
      0,
    );
  }


  mostrarError(control: AbstractControl): boolean {
    return control.invalid && (control.touched || control.dirty);
  }


  get errorFechas(): boolean {
    const { fechaDespacho, fechaEntregaEstimada } = this.form.controls;
    return (
      this.form.hasError('rangoFechasInvalido') &&
      (fechaDespacho.touched || fechaDespacho.dirty ||
        fechaEntregaEstimada.touched || fechaEntregaEstimada.dirty)
    );
  }



  registrar(): void {
    if (this.form.invalid || this.form.pending) {
      this.form.markAllAsTouched();
      return;
    }

    const valores = this.form.getRawValue();

    const payload: EnvioRegistroPayload = {
      numeroTracking: valores.numeroTracking.trim().toUpperCase(),
      destinatario: valores.destinatario.trim(),
      direccionDestino: valores.direccionDestino.trim(),
      montoFlete: Number(valores.montoFlete),
      fechaDespacho: valores.fechaDespacho,
      fechaEntregaEstimada: valores.fechaEntregaEstimada,
      paquetes: valores.paquetes.map((p) => ({
        descripcion: p.descripcion.trim(),
        pesoKg: Number(p.pesoKg),
      })),
    };

    this.enviando.set(true);
    this.error.set('');

    this.envioService.registrarEnvioAvanzado(payload).subscribe({
      next: (nuevo) => {
        this.creado.set(nuevo);
        this.enviando.set(false);
        this.reiniciarFormulario();
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, 'No se pudo registrar el envío.'));
        this.enviando.set(false);
      },
    });
  }

  registrarOtro(): void {
    this.creado.set(null);
  }


  private reiniciarFormulario(): void {
    this.paquetes.clear();
    this.paquetes.push(this.crearPaquete());
    this.form.reset(); 
  }
}