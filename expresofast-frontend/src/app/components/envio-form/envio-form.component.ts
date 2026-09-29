import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EnvioService } from '../../services/envio.service';
import { mensajeDeError } from '../../services/http-error';
import { CrearEnvioPayload, Envio } from '../../models/envio.model';

@Component({
  selector: 'app-envio-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './envio-form.component.html',
  styleUrl: './envio-form.component.css',
})
export class EnvioFormComponent {
  private envioService = inject(EnvioService);

  envio: CrearEnvioPayload = this.formularioVacio();

  enviando = signal(false);
  error = signal('');
  creado = signal<Envio | null>(null);

  registrar(form: NgForm): void {
    if (form.invalid) {
      form.control.markAllAsTouched();
      return;
    }

    this.enviando.set(true);
    this.error.set('');

    const payload: CrearEnvioPayload = {
      destinatario: this.envio.destinatario.trim(),
      direccionDestino: this.envio.direccionDestino.trim(),
      montoFlete: Number(this.envio.montoFlete),
    };

    this.envioService.crearEnvio(payload).subscribe({
      next: (nuevo) => {
        this.creado.set(nuevo);
        this.enviando.set(false);
        this.envio = this.formularioVacio();
        form.resetForm(this.envio);
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

  private formularioVacio(): CrearEnvioPayload {
    return { destinatario: '', direccionDestino: '', montoFlete: null };
  }
}
