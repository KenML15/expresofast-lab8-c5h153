import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EnvioService } from '../../services/envio.service';
import { mensajeDeError } from '../../services/http-error';
import {
  ESTADOS_ENVIO,
  ESTADOS_FINALES,
  Envio,
  EstadoEnvio,
  etiquetaEstado,
} from '../../models/envio.model';

type Filtro = 'TODOS' | EstadoEnvio;

@Component({
  selector: 'app-envio-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './envio-list.component.html',
  styleUrl: './envio-list.component.css',
})
export class EnvioListComponent implements OnInit {
  private envioService = inject(EnvioService);

  readonly estados = ESTADOS_ENVIO;
  readonly etiquetaEstado = etiquetaEstado;

  envios = signal<Envio[]>([]);
  cargando = signal(true);
  error = signal('');
  mensaje = signal('');
  actualizandoId = signal<number | null>(null);
  filtro = signal<Filtro>('TODOS');
  busqueda = signal('');

  enviosFiltrados = computed(() => {
    const filtro = this.filtro();
    const termino = this.busqueda().trim().toLowerCase();
    return this.envios().filter(
      (e) =>
        (filtro === 'TODOS' || e.estado === filtro) &&
        (!termino ||
          e.codigoRastreo.toLowerCase().includes(termino) ||
          e.destinatario?.toLowerCase().includes(termino) ||
          e.direccionDestino.toLowerCase().includes(termino))
    );
  });

  resumen = computed(() =>
    this.estados.map((estado) => ({
      ...estado,
      total: this.envios().filter((e) => e.estado === estado.valor).length,
    }))
  );

  ngOnInit(): void {
    this.cargarEnvios();
  }

  cargarEnvios(): void {
    this.cargando.set(true);
    this.error.set('');
    this.envioService.obtenerEnvios().subscribe({
      next: (data) => {
        this.envios.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set(mensajeDeError(err, 'No se pudieron cargar los envíos.'));
        this.cargando.set(false);
      },
    });
  }

  /** Un envío entregado o cancelado no puede volver a pendiente ni a tránsito (regla del backend). */
  opcionDeshabilitada(envio: Envio, estado: EstadoEnvio): boolean {
    return ESTADOS_FINALES.includes(envio.estado) && !ESTADOS_FINALES.includes(estado);
  }

  cambiarEstado(envio: Envio, selector: HTMLSelectElement): void {
    const nuevoEstado = selector.value as EstadoEnvio;
    if (nuevoEstado === envio.estado) {
      return;
    }

    this.mensaje.set('');
    this.error.set('');
    this.actualizandoId.set(envio.id);

    this.envioService.actualizarEstado(envio.id, nuevoEstado).subscribe({
      next: (actualizado) => {
        this.envios.update((lista) => lista.map((e) => (e.id === actualizado.id ? actualizado : e)));
        this.mensaje.set(
          `La guía ${actualizado.codigoRastreo} cambió a "${etiquetaEstado(actualizado.estado)}".`
        );
        this.actualizandoId.set(null);
      },
      error: (err) => {
        selector.value = envio.estado;
        this.error.set(mensajeDeError(err, 'No se pudo actualizar el estado.'));
        this.actualizandoId.set(null);
      },
    });
  }
}
