import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EnvioService } from '../../services/envio.service';
import { mensajeDeError } from '../../services/http-error';
import { Envio, EstadoEnvio, etiquetaEstado } from '../../models/envio.model';

interface PasoSeguimiento {
  estado: EstadoEnvio;
  titulo: string;
  descripcion: string;
}

@Component({
  selector: 'app-envio-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './envio-tracking.component.html',
  styleUrl: './envio-tracking.component.css',
})
export class EnvioTrackingComponent implements OnInit {
  private envioService = inject(EnvioService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  readonly etiquetaEstado = etiquetaEstado;
  readonly pasos: PasoSeguimiento[] = [
    { estado: 'PENDIENTE', titulo: 'Registrado', descripcion: 'La guía fue creada y espera despacho.' },
    { estado: 'EN_TRANSITO', titulo: 'En tránsito', descripcion: 'El paquete va en camino a su destino.' },
    { estado: 'ENTREGADO', titulo: 'Entregado', descripcion: 'El destinatario recibió el paquete.' },
  ];

  codigo = '';
  buscando = signal(false);
  error = signal('');
  envio = signal<Envio | null>(null);

  /** Índice del paso alcanzado; -1 cuando el envío fue cancelado. */
  pasoActual = computed(() => {
    const envio = this.envio();
    if (!envio || envio.estado === 'CANCELADO') {
      return -1;
    }
    return this.pasos.findIndex((p) => p.estado === envio.estado);
  });

  progreso = computed(() => {
    const envio = this.envio();
    if (!envio) {
      return 0;
    }
    if (envio.estado === 'CANCELADO') {
      return 100;
    }
    return Math.round(((this.pasoActual() + 1) / this.pasos.length) * 100);
  });

  ngOnInit(): void {
    const codigo = this.route.snapshot.queryParamMap.get('codigo');
    if (codigo) {
      this.codigo = codigo;
      this.buscar();
    }
  }

  buscar(): void {
    const codigo = this.codigo.trim().toUpperCase();
    if (!codigo) {
      this.error.set('Ingrese un código de rastreo, por ejemplo EXP-2026-1001.');
      this.envio.set(null);
      return;
    }

    this.codigo = codigo;
    this.buscando.set(true);
    this.error.set('');
    this.router.navigate([], { queryParams: { codigo }, replaceUrl: true });

    this.envioService.obtenerPorRastreo(codigo).subscribe({
      next: (envio) => {
        this.envio.set(envio);
        this.buscando.set(false);
      },
      error: (err) => {
        this.envio.set(null);
        this.error.set(mensajeDeError(err, 'No se encontró la guía.'));
        this.buscando.set(false);
      },
    });
  }
}
